package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;

public class InitCommandTest {

    // A group that no typical student belongs to, so the typical address book has not registered it.
    private static final Group T99 = new Group("T99");

    @Test
    public void constructor_nullGroup_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InitCommand(null));
    }

    @Test
    public void execute_newGroup_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addGroup(T99);
        expectedModel.setActiveGroup(T99);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_NO_PERSONS);

        assertCommandSuccess(new InitCommand(T99), model,
                String.format(InitCommand.MESSAGE_SUCCESS, T99), expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(T99, model.getActiveGroup().orElseThrow());
    }

    @Test
    public void execute_duplicateGroup_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addGroup(T99);
        model.setActiveGroup(T99);
        int displayedPersonCount = model.getFilteredPersonList().size();

        assertCommandFailure(new InitCommand(new Group("T99")), model,
                String.format(InitCommand.MESSAGE_DUPLICATE_GROUP, T99));
        assertEquals(T99, model.getActiveGroup().orElseThrow());
        assertEquals(displayedPersonCount, model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        InitCommand initT99 = new InitCommand(T99);
        InitCommand initT99Copy = new InitCommand(new Group("T99"));
        InitCommand initT10 = new InitCommand(new Group("T10"));

        assertTrue(initT99.equals(initT99));
        assertTrue(initT99.equals(initT99Copy));
        assertFalse(initT99.equals(initT10));
        assertFalse(initT99.equals(null));
    }
}
