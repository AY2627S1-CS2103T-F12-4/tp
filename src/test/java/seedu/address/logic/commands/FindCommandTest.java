package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private final Person johnny = new PersonBuilder().withName("Johnny Lim")
            .withEmail("johnnylim@example.com").withMatric("A0000011A").build();
    private final Person johnTan = new PersonBuilder().withName("John Tan")
            .withEmail("johntan@example.com").withMatric("A0000012B").build();
    private final Person johnson = new PersonBuilder().withName("Johnson Ng")
            .withEmail("johnsonng@example.com").withMatric("A0000013C").build();
    private final Person johnLim = new PersonBuilder().withName("John Lim")
            .withEmail("johnlim@example.com").withMatric("A0000014D").build();
    private final Person johnTanner = new PersonBuilder().withName("John Tanner")
            .withEmail("johntanner@example.com").withMatric("A0000015E").build();
    private final Person meiTan = new PersonBuilder().withName("Mei Tan")
            .withEmail("meitan@example.com").withMatric("A0000016F").build();
    private final AddressBook roster = new AddressBookBuilder().withPerson(johnny).withPerson(johnTan)
            .withPerson(johnson).withPerson(johnLim).withPerson(johnTanner).withPerson(meiTan).build();
    private final Model model = new ModelManager(roster, new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void constructor_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        FindCommand command = new FindCommand(new NameContainsQueryPredicate("John"));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_mixedCase_ordersByMatricWithoutReorderingRoster() throws Exception {
        AddressBook unsortedRoster = new AddressBookBuilder().withPerson(johnLim).withPerson(johnTanner)
                .withPerson(johnTan).withPerson(johnny).withPerson(johnson).build();
        Model unsortedModel = new ModelManager(unsortedRoster, new UserPrefs());
        CommandResult result = parser.parseCommand("find n/jOhN").execute(unsortedModel);

        assertEquals("Found 5 student(s) matching \"jOhN\".", result.getFeedbackToUser());
        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner), unsortedModel.getFilteredPersonList());
        assertEquals(unsortedRoster, unsortedModel.getAddressBook());
    }

    @Test
    public void execute_multipleWords_matchesContiguousPhraseInMatricOrder() throws Exception {
        Model phraseModel = new ModelManager(new AddressBookBuilder().withPerson(johnTanner)
                .withPerson(johnLim).withPerson(johnTan).withPerson(meiTan).build(), new UserPrefs());
        CommandResult result = parser.parseCommand("find n/ John   Tan ").execute(phraseModel);

        assertEquals("Found 2 student(s) matching \"John Tan\".", result.getFeedbackToUser());
        assertEquals(List.of(johnTan, johnTanner), phraseModel.getFilteredPersonList());
    }

    @Test
    public void execute_reversedPhrase_noStudentsFound() throws Exception {
        CommandResult result = parser.parseCommand("find n/tan john").execute(model);

        assertEquals("No students found matching \"tan john\". Try another name.", result.getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_partialOnly_ordersByMatric() throws Exception {
        parser.parseCommand("find n/joh").execute(model);

        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_afterUnmatchedSearch_searchesEntireRoster() throws Exception {
        parser.parseCommand("find n/UnknownStudent").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());

        parser.parseCommand("find n/Mei").execute(model);
        assertEquals(List.of(meiTan), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterMatchingSearch_replacesPreviousResults() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        parser.parseCommand("find n/joh").execute(model);
        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner), model.getFilteredPersonList());

        parser.parseCommand("find n/Mei").execute(model);
        assertEquals(List.of(meiTan), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_emptyRoster_reportsNoResults() throws Exception {
        Model emptyModel = new ModelManager();
        CommandResult result = parser.parseCommand("find n/John").execute(emptyModel);

        assertEquals("No students found matching \"John\". Try another name.", result.getFeedbackToUser());
        assertEquals(List.of(), emptyModel.getFilteredPersonList());
    }

    @Test
    public void parse_invalidSearch_preservesPreviousResultsAndRoster() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        for (String input : new String[]{"find John", "find n/", "find n/John n/Tan",
            "find n/John grp/T09", "find n/John123", "find\u00a0n/John\u2003grp/T09",
            "find n/John\n n/Tan", "find\u2003n/John2"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner), model.getFilteredPersonList());
            assertEquals(roster, model.getAddressBook());
        }
    }

    @Test
    public void execute_listAfterSearch_restoresMatricOrderInSameObservableView() throws Exception {
        ObservableList<Person> displayed = model.getFilteredPersonList();
        parser.parseCommand("find n/John").execute(model);
        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner), displayed);

        parser.parseCommand("list").execute(model);
        assertSame(displayed, model.getFilteredPersonList());
        assertEquals(roster.getPersonList(), displayed);
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_deleteAfterSearch_deletesDisplayedStudentAndUpdatesIndices() throws Exception {
        ObservableList<Person> displayed = model.getFilteredPersonList();
        parser.parseCommand("find n/John").execute(model);
        parser.parseCommand("delete 1").execute(model);

        assertFalse(model.getAddressBook().getPersonList().contains(johnny));
        assertTrue(model.hasPerson(johnTan));
        assertEquals(List.of(johnTan, johnson, johnLim, johnTanner), displayed);
        assertEquals(List.of(johnTan, johnson, johnLim, johnTanner, meiTan), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_editAfterSearch_editsDisplayedStudentAndResetsView() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        parser.parseCommand("edit 1 n/Jack Tan").execute(model);
        Person edited = new PersonBuilder(johnny).withName("Jack Tan").build();

        assertFalse(model.getAddressBook().getPersonList().contains(johnny));
        assertTrue(model.hasPerson(johnTan));
        assertEquals(List.of(edited, johnTan, johnson, johnLim, johnTanner, meiTan), model.getFilteredPersonList());
    }

    @Test
    public void execute_addAfterSearch_preservesResultsAndMatricOrder() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        Person alice = new PersonBuilder().withName("Alice").withEmail("alice@example.com").build();
        new AddCommand(alice).execute(model);

        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner),
                model.getFilteredPersonList());
    }

    @Test
    public void execute_markAfterSearch_usesDisplayedStudentsOwnGroupSession() throws Exception {
        Person otherGroupJohnny = new PersonBuilder(johnny).withGroup("T10").build();
        AddressBook book = new AddressBookBuilder().withPerson(johnTan).withPerson(otherGroupJohnny).build();
        Session firstGroupSession = new Session(new Group("T09"), new Week(5), new SessionDate("2026-09-15"));
        Session secondGroupSession = new Session(new Group("T10"), new Week(5), new SessionDate("2026-09-16"));
        book.addSession(firstGroupSession);
        book.addSession(secondGroupSession);
        Model searchModel = new ModelManager(book, new UserPrefs());

        parser.parseCommand("find n/john").execute(searchModel);
        parser.parseCommand("mark 1 w/5 s/present").execute(searchModel);

        Person marked = searchModel.getFilteredPersonList().getFirst();
        assertEquals(otherGroupJohnny.getMatric(), marked.getMatric());
        assertEquals(Status.PRESENT, marked.getAttendance(secondGroupSession).orElseThrow().getStatus());
        assertTrue(marked.getAttendance(firstGroupSession).isEmpty());
        assertTrue(searchModel.getFilteredPersonList().getLast().getAttendances().isEmpty());
    }

    @Test
    public void execute_searchThenSave_preservesStoredRosterOrder(@TempDir Path temporaryFolder) throws Exception {
        AddressBook unsortedRoster = new AddressBookBuilder().withPerson(johnTan).withPerson(johnny)
                .withPerson(meiTan).build();
        Model searchModel = new ModelManager(unsortedRoster, new UserPrefs());
        parser.parseCommand("find n/john").execute(searchModel);
        assertEquals(List.of(johnny, johnTan), searchModel.getFilteredPersonList());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        storage.saveAddressBook(searchModel.getAddressBook());
        assertEquals(unsortedRoster, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void equals() {
        FindCommand first = new FindCommand(new NameContainsQueryPredicate("first"));
        FindCommand second = new FindCommand(new NameContainsQueryPredicate("second"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(new FindCommand(new NameContainsQueryPredicate("first"))));
        assertFalse(first.equals(1));
        assertFalse(first.equals(null));
        assertFalse(first.equals(second));
    }

    @Test
    public void toStringMethod() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("keyword");
        FindCommand command = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, command.toString());
    }
}
