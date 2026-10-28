package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests what {@code find} says when nothing matches exactly but a student's name comes close.
 * The suggestions are feedback only: the displayed list stays empty, so a mistyped search can
 * never leave students on screen for an index-based command to act on by accident.
 */
public class FindSuggestionsTest {

    private final Person sarah = new PersonBuilder().withName("Sarah Tan").withMatric("A0000001X")
            .withEmail("sarah@example.com").withGroup("T09").build();
    private final Person sandra = new PersonBuilder().withName("Sandra Khoo").withMatric("A0000002X")
            .withEmail("sandra@example.com").withGroup("T09").build();
    private final Person benjamin = new PersonBuilder().withName("Benjamin Lee").withMatric("A0000003X")
            .withEmail("ben@example.com").withGroup("T10").build();
    private final AddressBook roster = new AddressBookBuilder().withPerson(sarah).withPerson(sandra)
            .withPerson(benjamin).build();
    private final ModelManager model = new ModelManager(roster, new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_mistypedName_suggestsTheIntendedStudent() throws Exception {
        CommandResult result = run("find n/sarh");

        assertEquals(String.format(FindCommand.MESSAGE_DID_YOU_MEAN, "sarh", "Sarah Tan (A0000001X)"),
                result.getFeedbackToUser());
        // The suggestion is a hint, not a result: nothing is displayed to act on
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_transposedLetters_suggestsTheIntendedStudent() throws Exception {
        // Swapping two letters is the commonest typo, and it lands past the shared prefix where
        // the similarity bonus is smallest, so this is the case the threshold has to admit.
        CommandResult result = run("find n/srah");

        assertEquals(String.format(FindCommand.MESSAGE_DID_YOU_MEAN, "srah", "Sarah Tan (A0000001X)"),
                result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_exactMatchExists_saysNothingAboutSimilarNames() throws Exception {
        CommandResult result = run("find n/Sarah");

        assertEquals(String.format(FindCommand.MESSAGE_SUCCESS, 1, "Sarah"), result.getFeedbackToUser());
        assertEquals(List.of(sarah), model.getFilteredPersonList());
    }

    @Test
    public void execute_nothingCloseEnough_keepsThePlainNoResultsMessage() throws Exception {
        CommandResult result = run("find n/zzzz");

        assertEquals(String.format(FindCommand.MESSAGE_NO_RESULTS, "zzzz"), result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_noNameFilter_neverSuggests() throws Exception {
        // There is no name to have mistyped, so a missing matriculation number is just a miss
        CommandResult result = run("find m/A0000009Z");

        assertEquals(String.format(FindCommand.MESSAGE_FILTER_NO_RESULTS, "m/A0000009Z"),
                result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_mistypedNameWithGroupFilter_suggestsOnlyWithinThatGroup() throws Exception {
        CommandResult result = run("find n/sarh grp/T10");

        // Sarah is in T09, so the group filter rules her out of the suggestions as well
        assertEquals(String.format(FindCommand.MESSAGE_FILTER_NO_RESULTS, "n/\"sarh\" grp/T10"),
                result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_mistypedNameWithMatchingGroup_suggestsWithinThatGroup() throws Exception {
        CommandResult result = run("find n/sarh grp/T09");

        assertEquals(String.format(FindCommand.MESSAGE_FILTER_DID_YOU_MEAN,
                "n/\"sarh\" grp/T09", "Sarah Tan (A0000001X)"), result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_severalCloseNames_suggestsAtMostThreeClosestFirst() throws Exception {
        ModelManager crowded = new ModelManager(new AddressBookBuilder()
                .withPerson(new PersonBuilder().withName("Sarah").withMatric("A0000001X")
                        .withEmail("a@example.com").withGroup("T09").build())
                .withPerson(new PersonBuilder().withName("Sarahh").withMatric("A0000002X")
                        .withEmail("b@example.com").withGroup("T09").build())
                .withPerson(new PersonBuilder().withName("Sarrah").withMatric("A0000003X")
                        .withEmail("c@example.com").withGroup("T09").build())
                .withPerson(new PersonBuilder().withName("Saraah").withMatric("A0000004X")
                        .withEmail("d@example.com").withGroup("T09").build())
                .build(), new UserPrefs());

        String feedback = parser.parseCommand("find n/sarahx").execute(crowded).getFeedbackToUser();

        assertTrue(feedback.startsWith("No students found matching \"sarahx\". Did you mean: "));
        assertEquals(3, feedback.split(", ").length);
        assertFalse(crowded.getFilteredPersonList().iterator().hasNext());
    }

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }
}
