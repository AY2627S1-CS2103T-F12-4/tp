package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.ListCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.AttendanceCell;

/**
 * Exercises group listing with the shared displayed indices used by other commands.
 */
public class GroupListIntegrationTest {
    private final Group group = new Group("T09");
    private final Person later = new PersonBuilder().withName("Later Student").withMatric("A0000009Z")
            .withGroup("T09").build();
    private final Person earlier = new PersonBuilder().withName("Earlier Student").withMatric("A0000002Z")
            .withGroup("T09").build();
    private final Person otherGroup = new PersonBuilder().withName("Other Student").withMatric("A0000001Z")
            .withGroup("T10").build();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        AddressBook book = new AddressBook();
        book.addGroup(group);
        book.addGroup(new Group("T10"));
        book.addPerson(later);
        book.addPerson(otherGroup);
        book.addPerson(earlier);
        book.addGroup(new Group("T11"));
        book.addSession(T09_WEEK_1);
        model = new ModelManager(book, new UserPrefs());
    }

    @Test
    public void execute_findThenList_restoresMatricOrderAndSharesGroupState() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("list grp/T09").execute(model);
        assertEquals(group, model.getActiveGroup().orElseThrow());
        assertEquals(group, model.activeGroupProperty().get());

        parser.parseCommand("find n/Student").execute(model);
        assertEquals(List.of(later, otherGroup, earlier), model.getFilteredPersonList());
        assertTrue(model.getActiveGroup().isEmpty());
        assertNull(model.activeGroupProperty().get());

        parser.parseCommand("mark 1 w/1 s/present").execute(model);
        Person marked = model.getFilteredPersonList().getFirst();
        assertEquals(later.getMatric(), marked.getMatric());
        assertEquals(List.of(new AttendanceCell(1, "P")),
                AttendanceCell.forStudent(marked, model.getSessionList()));

        parser.parseCommand("list grp/T09").execute(model);
        assertEquals(List.of(earlier, marked), model.getFilteredPersonList());
        assertEquals(group, model.getActiveGroup().orElseThrow());
        parser.parseCommand("list").execute(model);
        assertEquals(List.of(otherGroup, earlier, marked), model.getFilteredPersonList());
        assertTrue(model.getActiveGroup().isEmpty());
    }

    @Test
    public void execute_initThenFind_preservesOrClearsScopeAsAppropriate() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        Group empty = new Group("T12");
        parser.parseCommand("init grp/T12").execute(model);
        assertEquals(empty, model.getActiveGroup().orElseThrow());
        assertEquals(empty, model.activeGroupProperty().get());
        assertTrue(model.getFilteredPersonList().isEmpty());

        assertThrows(ParseException.class, () -> parser.parseCommand("find n/"));
        assertEquals(empty, model.getActiveGroup().orElseThrow());
        assertTrue(model.getFilteredPersonList().isEmpty());

        parser.parseCommand("find n/Missing").execute(model);
        assertTrue(model.getActiveGroup().isEmpty());
        assertNull(model.activeGroupProperty().get());
        parser.parseCommand("list grp/T12").execute(model);
        parser.parseCommand("find n/Other").execute(model);
        assertEquals(List.of(otherGroup), model.getFilteredPersonList());
        assertTrue(model.getActiveGroup().isEmpty());
    }

    @Test
    public void execute_sessionCreation_refreshesCellsWithoutChangingGroupView() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("list grp/T09").execute(model);
        parser.parseCommand("session grp/T09 w/2 d/2026-08-18").execute(model);
        assertEquals(List.of(earlier, later), model.getFilteredPersonList());
        assertEquals(group, model.activeGroupProperty().get());
        assertEquals(List.of(new AttendanceCell(1, "\u2014"), new AttendanceCell(2, "\u2014")),
                AttendanceCell.forStudent(earlier, model.getSessionList()));
    }

    @Test
    public void execute_allAndGroupLists_sortByMatricAndSetScope() throws Exception {
        assertEquals("Listed 2 students in T09.", new ListCommand(group).execute(model).getFeedbackToUser());
        assertEquals(List.of(earlier, later), model.getFilteredPersonList());
        assertEquals(group, model.activeGroupProperty().get());
        assertEquals("Listed all 3 students.", new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(otherGroup, earlier, later), model.getFilteredPersonList());
        assertNull(model.activeGroupProperty().get());
    }

    @Test
    public void execute_emptyGroup_isDifferentFromUnknownGroup() throws Exception {
        Group empty = new Group("T11");
        assertEquals("Tutorial group T11 has no students yet.",
                new ListCommand(empty).execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(empty, model.activeGroupProperty().get());
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class,
                "Tutorial group T99 does not exist. Create it first with: init grp/T99", () ->
                    new ListCommand(new Group("T99")).execute(model));
        assertEquals(empty, model.activeGroupProperty().get());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_invalidInput_preservesExistingView() throws Exception {
        new ListCommand(group).execute(model);
        assertThrows(ParseException.class, () -> new ListCommandParser().parse(" grp/T09 grp/T10"));
        assertEquals(group, model.activeGroupProperty().get());
        assertEquals(List.of(earlier, later), model.getFilteredPersonList());
    }

    @Test
    public void execute_findSearchesAllGroups_andClearsActiveGroup() throws Exception {
        new ListCommand(group).execute(model);
        new FindCommand(new NameContainsQueryPredicate("Other")).execute(model);
        assertNull(model.activeGroupProperty().get());
        assertEquals(List.of(otherGroup), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteUsesSortedFilteredIndex_andKeepsEmptyGroup() throws Exception {
        new ListCommand(group).execute(model);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertFalse(model.hasPerson(earlier));
        assertTrue(model.hasPerson(otherGroup));
        assertEquals(List.of(later), model.getFilteredPersonList());
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertTrue(model.hasGroup(group));
        assertEquals("Tutorial group T09 has no students yet.",
                new ListCommand(group).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_markUsesSortedFilteredIndex_andRefreshesAttendance() throws Exception {
        new ListCommand(group).execute(model);
        new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT).execute(model);
        Person marked = model.getFilteredPersonList().getFirst();
        assertEquals(earlier.getMatric(), marked.getMatric());
        assertEquals(List.of(new AttendanceCell(1, "P")),
                AttendanceCell.forStudent(marked, model.getAddressBook().getSessionList()));
        assertEquals(List.of(new AttendanceCell(1, "\u2014")),
                AttendanceCell.forStudent(later, model.getAddressBook().getSessionList()));
        assertEquals(group, model.activeGroupProperty().get());
        model.addSession(new Session(group, new Week(2), new SessionDate("2026-08-18")));
        assertEquals(List.of(new AttendanceCell(1, "P"), new AttendanceCell(2, "\u2014")),
                AttendanceCell.forStudent(marked, model.getAddressBook().getSessionList()));
    }

    @Test
    public void addPerson_preservesFilterAndUpdatesSortedIndices() throws Exception {
        new ListCommand(group).execute(model);
        Person first = new PersonBuilder().withName("First Student").withMatric("A0000000Z")
                .withGroup("T09").build();
        model.addPerson(first);
        assertEquals(List.of(first, earlier, later), model.getFilteredPersonList());
        Person hidden = new PersonBuilder().withName("Hidden Student").withMatric("A0000003Z")
                .withGroup("T10").build();
        model.addPerson(hidden);
        assertEquals(List.of(first, earlier, later), model.getFilteredPersonList());
        assertEquals(group, model.activeGroupProperty().get());
    }
}
