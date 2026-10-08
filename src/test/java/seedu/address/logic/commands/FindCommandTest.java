package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private final Person johnny = new PersonBuilder().withName("Johnny Lim").build();
    private final Person johnTan = new PersonBuilder().withName("John Tan").build();
    private final Person johnson = new PersonBuilder().withName("Johnson Ng").build();
    private final Person johnLim = new PersonBuilder().withName("John Lim").build();
    private final Person johnTanner = new PersonBuilder().withName("John Tanner").build();
    private final Person meiTan = new PersonBuilder().withName("Mei Tan").build();
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
    public void execute_mixedCase_ranksWholeWordMatchesBeforePartialMatches() throws Exception {
        CommandResult result = parser.parseCommand("find n/jOhN").execute(model);

        assertEquals("Found 5 student(s) matching \"jOhN\".", result.getFeedbackToUser());
        assertEquals(List.of(johnTan, johnLim, johnTanner, johnny, johnson), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_multipleWords_matchesContiguousPhraseWithWholeWordsFirst() throws Exception {
        // Place the partial phrase match first to ensure ranking is actually applied.
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
    public void execute_partialOnly_preservesOrderWithinMatchingTier() throws Exception {
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
    public void execute_afterMatchingSearch_replacesPreviousResultsAndRanking() throws Exception {
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
            "find n/John grp/T09", "find n/John123"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(List.of(johnTan, johnLim, johnTanner, johnny, johnson), model.getFilteredPersonList());
            assertEquals(roster, model.getAddressBook());
        }
    }

    @Test
    public void execute_listAfterSearch_restoresRosterOrderInSameObservableView() throws Exception {
        ObservableList<Person> displayed = model.getFilteredPersonList();
        parser.parseCommand("find n/John").execute(model);
        assertEquals(List.of(johnTan, johnLim, johnTanner, johnny, johnson), displayed);

        parser.parseCommand("list").execute(model);
        assertSame(displayed, model.getFilteredPersonList());
        assertEquals(roster.getPersonList(), displayed);
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_deleteAfterSearch_deletesDisplayedStudentAndUpdatesRanking() throws Exception {
        ObservableList<Person> displayed = model.getFilteredPersonList();
        parser.parseCommand("find n/John").execute(model);
        parser.parseCommand("delete 1").execute(model);

        assertFalse(model.hasPerson(johnTan));
        assertTrue(model.hasPerson(johnny));
        assertEquals(List.of(johnLim, johnTanner, johnny, johnson), displayed);
        assertEquals(List.of(johnny, johnson, johnLim, johnTanner, meiTan), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_editAfterSearch_editsDisplayedStudentAndResetsView() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        parser.parseCommand("edit 1 n/Jack Tan").execute(model);
        Person edited = new PersonBuilder(johnTan).withName("Jack Tan").build();

        assertFalse(model.hasPerson(johnTan));
        assertTrue(model.hasPerson(johnny));
        assertEquals(List.of(johnny, edited, johnson, johnLim, johnTanner, meiTan), model.getFilteredPersonList());
    }

    @Test
    public void execute_addAfterSearch_restoresUnfilteredRosterOrder() throws Exception {
        parser.parseCommand("find n/John").execute(model);
        Person alice = new PersonBuilder().withName("Alice").build();
        new AddCommand(alice).execute(model);

        assertEquals(List.of(johnny, johnTan, johnson, johnLim, johnTanner, meiTan, alice),
                model.getFilteredPersonList());
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
