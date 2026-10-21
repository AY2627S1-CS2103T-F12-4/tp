package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class FilteredFindCommandTest {
    @TempDir
    public Path temporaryFolder;

    private final Person john = new PersonBuilder().withName("John Tan").withMatric("A0000002X")
            .withEmail("john@example.com").withGroup("T09").build();
    private final Person johnny = new PersonBuilder().withName("Johnny Lim").withMatric("A0000001X")
            .withEmail("johnny@example.com").withGroup("T10").build();
    private final Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000003X")
            .withEmail("alice@example.com").withGroup("T09").build();
    private final AddressBook roster = new AddressBookBuilder().withPerson(john).withPerson(alice)
            .withPerson(johnny).build();
    private final ModelManager model = new ModelManager(roster, new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_modelRetainsGroupSelection_detectsViolationOfGlobalSearchContract() {
        ModelManager inconsistentModel = new ModelManager(roster, new UserPrefs()) {
            @Override
            public Optional<Group> getActiveGroup() {
                // Simulate a model implementation that exposes a stale selection after filtering.
                return Optional.of(new Group("T09"));
            }
        };
        assertThrows(AssertionError.class, () -> parser.parseCommand("find n/John").execute(inconsistentModel));
    }

    @Test
    public void execute_eachFieldAndAllFilters_matchesExactFieldsAndCombinesWithAnd() throws Exception {
        run("find grp/t09");
        assertEquals(List.of(john, alice), model.getFilteredPersonList());
        assertTrue(model.getActiveGroup().isEmpty());

        run("find m/a0000001x");
        assertEquals(List.of(johnny), model.getFilteredPersonList());

        CommandResult result = run("find grp/t09 m/a0000002x n/ jOhN ");
        assertEquals(List.of(john), model.getFilteredPersonList());
        assertEquals("Found 1 student(s) matching filters: n/\"jOhN\" m/A0000002X grp/T09.",
                result.getFeedbackToUser());

        result = run("find n/John m/A0000001X grp/T09");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals("No students found matching filters: n/\"John\" m/A0000001X grp/T09. Try another filter.",
                result.getFeedbackToUser());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_afterGroupViewAndEmptyResults_searchesCompleteRoster() throws Exception {
        run("list grp/T09");
        run("find n/Unknown grp/T09");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.getActiveGroup().isEmpty());
        run("find n/John grp/T10");
        assertEquals(List.of(johnny), model.getFilteredPersonList());
        run("find n/John");
        assertEquals(List.of(johnny, john), model.getFilteredPersonList());
    }

    @Test
    public void execute_unknownGroupAndInvalidQueries_preserveDataResultsAndSelection() throws Exception {
        run("list grp/T09");
        CommandException failure = assertThrows(CommandException.class, () -> run("find n/John grp/T99"));
        assertEquals(String.format(ListCommand.MESSAGE_GROUP_NOT_FOUND, "T99"), failure.getMessage());
        for (String query : List.of("find grp/T9", "find n/John grp/", "find m/A0000002X m/A0000001X",
                "find n/John x/foo", "find n/John grp/T09 grp/T10")) {
            assertThrows(ParseException.class, () -> run(query));
        }
        assertEquals(List.of(john, alice), model.getFilteredPersonList());
        assertEquals(new Group("T09"), model.getActiveGroup().orElseThrow());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_registeredEmptyGroup_returnsEmptyResults() throws Exception {
        model.addGroup(new Group("TG01"));
        run("list grp/T09");
        assertEquals("No students found matching filters: grp/TG01. Try another filter.",
                run("find grp/TG01").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.getActiveGroup().isEmpty());
    }

    @Test
    public void execute_addAndDeleteAfterCombinedSearch_keepEveryFilterAndCorrectIndexes() throws Exception {
        run("find n/John grp/T09");
        new AddCommand(new PersonBuilder().withName("Other Student").withMatric("A0000004X")
                .withEmail("other@example.com").build()).execute(model);
        new AddCommand(new PersonBuilder().withName("John Other Group").withMatric("A0000005X")
                .withEmail("another@example.com").withGroup("T10").build()).execute(model);
        assertEquals(List.of(john), model.getFilteredPersonList());

        Person added = new PersonBuilder().withName("John New").withMatric("A0000000X")
                .withEmail("new@example.com").build();
        new AddCommand(added).execute(model);
        assertEquals(List.of(added, john), model.getFilteredPersonList());
        run("delete 1");
        assertFalse(model.hasPerson(added));
        assertEquals(List.of(john), model.getFilteredPersonList());
    }

    @Test
    public void execute_markAndEditAfterSearch_useDisplayedStudentAndExistingEditReset() throws Exception {
        Session session = new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-12"));
        model.addSession(session);
        run("find n/John grp/T10");
        run("mark 1 w/1 s/present");
        Person marked = model.getFilteredPersonList().getFirst();
        assertEquals(johnny.getMatric(), marked.getMatric());
        assertEquals(Status.PRESENT, marked.getAttendance(session).orElseThrow().getStatus());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getAttendances().isEmpty());
        run("edit 1 n/James Lim");
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals("James Lim", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_listCommands_replaceSearchFilters() throws Exception {
        run("find n/John grp/T09");
        run("list grp/T10");
        assertEquals(List.of(johnny), model.getFilteredPersonList());
        assertEquals(new Group("T10"), model.getActiveGroup().orElseThrow());
        run("list");
        assertEquals(List.of(johnny, john, alice), model.getFilteredPersonList());
        assertTrue(model.getActiveGroup().isEmpty());
    }

    @Test
    public void execute_throughLogic_doesNotSaveRosterForFind() throws Exception {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Search must not save the roster");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("find n/John grp/T09");
        assertEquals(List.of(john), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }
}
