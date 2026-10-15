package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, validPerson.getName(),
                        validPerson.getMatric(), validPerson.getGroup()),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, personInList.getMatric()));
    }

    @Test
    public void execute_invalidNameOrUnknownParameter_preservesRosterAndView() {
        Group group = new Group("T09");
        model.showGroup(group);
        AddressBook previousBook = new AddressBook(model.getAddressBook());
        List<Person> previousResults = List.copyOf(model.getFilteredPersonList());
        AddressBookParser parser = new AddressBookParser();

        for (String args : new String[]{"n/123", "n/Alex-", "n/" + "A".repeat(71), "n/Alex x/ignored"}) {
            String command = "add " + args + " p/91234567 e/alex@example.com"
                    + " a/Kent Ridge m/A0099999X grp/T09";
            assertThrows(ParseException.class, () -> parser.parseCommand(command).execute(model));
            assertEquals(previousBook, model.getAddressBook());
            assertEquals(previousResults, model.getFilteredPersonList());
            assertEquals(group, model.getActiveGroup().orElseThrow());
        }

        String unknownAfterAddress = "add n/Alex p/91234567 e/alex@example.com"
                + " a/Kent Ridge x/ignored m/A0099999X grp/T09";
        assertThrows(ParseException.class, () -> parser.parseCommand(unknownAfterAddress).execute(model));
        assertEquals(previousBook, model.getAddressBook());
        assertEquals(previousResults, model.getFilteredPersonList());
        assertEquals(group, model.getActiveGroup().orElseThrow());
    }

}
