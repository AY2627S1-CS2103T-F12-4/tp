package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalSessions.getTypicalSessions;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) for {@code DeleteAllCommand}.
 * In the typical address book, ALICE to CARL are in T09 and DANIEL to GEORGE are in T10.
 */
public class DeleteAllCommandTest {

    private static final Group T09 = new Group("T09");
    private static final Group T10 = new Group("T10");

    private final Model model = new ModelManager(getTypicalAddressBookWithSessions(), new UserPrefs());

    private static AddressBook getTypicalAddressBookWithSessions() {
        AddressBook addressBook = getTypicalAddressBook();
        getTypicalSessions().forEach(addressBook::addSession);
        return addressBook;
    }

    @Test
    public void execute_oneGroupDisplayed_deletesOnlyThatGroupsStudents() {
        model.showGroup(T09);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.showGroup(T09);
        List.copyOf(expectedModel.getFilteredPersonList()).forEach(expectedModel::deletePerson);

        assertCommandSuccess(new DeleteAllCommand(), model, String.format(DeleteAllCommand.MESSAGE_SUCCESS, 3),
                expectedModel);
        assertTrue(model.getAddressBook().getPersonList().stream().allMatch(p -> p.getGroup().equals(T10)));
    }

    @Test
    public void execute_allStudentsDisplayed_keepsGroupsAndSessions() throws Exception {
        List<Group> groupsBefore = List.copyOf(model.getGroupList());
        int sessionCountBefore = model.getSessionList().size();

        CommandResult result = new DeleteAllCommand().execute(model);

        assertEquals(String.format(DeleteAllCommand.MESSAGE_SUCCESS, 7), result.getFeedbackToUser());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(groupsBefore, model.getGroupList());
        assertEquals(sessionCountBefore, model.getSessionList().size());
    }

    @Test
    public void execute_filteredBySearch_deletesOnlyMatchingStudents() throws Exception {
        Person alice = model.getFilteredPersonList().get(0);
        model.updateFilteredPersonList(person -> person.equals(alice));

        new DeleteAllCommand().execute(model);

        assertFalse(model.hasPerson(alice));
        assertEquals(6, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_noStudentsDisplayed_failure() {
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_NO_PERSONS);
        assertCommandFailure(new DeleteAllCommand(), model, DeleteAllCommand.MESSAGE_NO_STUDENTS);
    }

    @Test
    public void equals() {
        DeleteAllCommand deleteAllCommand = new DeleteAllCommand();

        assertTrue(deleteAllCommand.equals(deleteAllCommand));
        assertTrue(deleteAllCommand.equals(new DeleteAllCommand()));
        assertFalse(deleteAllCommand.equals(null));
        assertFalse(deleteAllCommand.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        assertEquals(DeleteAllCommand.class.getCanonicalName(), new DeleteAllCommand().toString());
    }
}
