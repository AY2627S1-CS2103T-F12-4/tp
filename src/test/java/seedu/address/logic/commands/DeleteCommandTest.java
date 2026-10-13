package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.ModelStub;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code DeleteCommand}.
 * The unit tests run the command against a stub model, so they check only the command's own behavior.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        // ALICE is the first typical person
        String expectedMessage = "Deleted Alice Pauline (A0000001A) from tutorial group T09, "
                + "including all attendance records.";

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = DeleteCommand.generateSuccessMessage(personToDelete);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexWithStubModel_deletesOnlyThatStudent() throws Exception {
        ModelStubWithDisplayedPersons modelStub = new ModelStubWithDisplayedPersons(ALICE, BENSON, CARL);

        CommandResult commandResult = new DeleteCommand(INDEX_SECOND_PERSON).execute(modelStub);

        assertEquals("Deleted Benson Meier (A0000002B) from tutorial group T09, including all attendance records.",
                commandResult.getFeedbackToUser());
        assertEquals(List.of(BENSON), modelStub.deletedPersons);
    }

    @Test
    public void execute_filteredListWithStubModel_deletesStudentAtDisplayedIndex() throws Exception {
        // The displayed list is filtered, so index 1 is CARL even though ALICE is first in the address book
        ModelStubWithDisplayedPersons modelStub = new ModelStubWithDisplayedPersons(CARL, ALICE);

        new DeleteCommand(INDEX_FIRST_PERSON).execute(modelStub);

        assertEquals(List.of(CARL), modelStub.deletedPersons);
    }

    @Test
    public void execute_indexOutOfRangeWithStubModel_throwsCommandExceptionAndDeletesNothing() {
        ModelStubWithDisplayedPersons modelStub = new ModelStubWithDisplayedPersons(ALICE);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                deleteCommand.execute(modelStub));
        assertTrue(modelStub.deletedPersons.isEmpty());
    }

    @Test
    public void execute_emptyDisplayedListWithStubModel_throwsCommandExceptionAndDeletesNothing() {
        ModelStubWithDisplayedPersons modelStub = new ModelStubWithDisplayedPersons();
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                deleteCommand.execute(modelStub));
        assertTrue(modelStub.deletedPersons.isEmpty());
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    /**
     * A Model stub that displays a fixed list of persons and records the persons deleted from it.
     * Any other call to the model fails the test.
     */
    private static class ModelStubWithDisplayedPersons extends ModelStub {
        private final ObservableList<Person> displayedPersons;
        private final List<Person> deletedPersons = new ArrayList<>();

        ModelStubWithDisplayedPersons(Person... displayedPersons) {
            this.displayedPersons = FXCollections.observableArrayList(displayedPersons);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.unmodifiableObservableList(displayedPersons);
        }

        @Override
        public void deletePerson(Person target) {
            requireNonNull(target);
            deletedPersons.add(target);
        }
    }
}
