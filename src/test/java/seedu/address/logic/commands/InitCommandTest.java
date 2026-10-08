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

    private static final Group T11 = new Group("T11");

    @Test
    public void constructor_nullGroup_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InitCommand(null));
    }

    @Test
    public void execute_newGroup_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addGroup(T11);
        expectedModel.showGroup(T11);

        assertCommandSuccess(new InitCommand(T11), model,
                String.format(InitCommand.MESSAGE_SUCCESS, T11), expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(T11, model.getActiveGroup().orElseThrow());
    }

    @Test
    public void execute_duplicateGroup_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addGroup(T11);
        model.setActiveGroup(T11);
        int displayedPersonCount = model.getFilteredPersonList().size();

        assertCommandFailure(new InitCommand(new Group("T11")), model,
                String.format(InitCommand.MESSAGE_DUPLICATE_GROUP, T11));
        assertEquals(T11, model.getActiveGroup().orElseThrow());
        assertEquals(displayedPersonCount, model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        InitCommand initT11 = new InitCommand(T11);
        InitCommand initT11Copy = new InitCommand(new Group("T11"));
        InitCommand initT10 = new InitCommand(new Group("T10"));

        assertTrue(initT11.equals(initT11));
        assertTrue(initT11.equals(initT11Copy));
        assertFalse(initT11.equals(initT10));
        assertFalse(initT11.equals(null));
    }
}
