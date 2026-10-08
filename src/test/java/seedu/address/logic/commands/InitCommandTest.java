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

    private static final Group T09 = new Group("T09");

    @Test
    public void constructor_nullGroup_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InitCommand(null));
    }

    @Test
    public void execute_newGroup_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addGroup(T09);
        expectedModel.setActiveGroup(T09);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_NO_PERSONS);

        assertCommandSuccess(new InitCommand(T09), model,
                String.format(InitCommand.MESSAGE_SUCCESS, T09), expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(T09, model.getActiveGroup().orElseThrow());
    }

    @Test
    public void execute_duplicateGroup_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addGroup(T09);
        model.setActiveGroup(T09);
        int displayedPersonCount = model.getFilteredPersonList().size();

        assertCommandFailure(new InitCommand(new Group("T09")), model,
                String.format(InitCommand.MESSAGE_DUPLICATE_GROUP, T09));
        assertEquals(T09, model.getActiveGroup().orElseThrow());
        assertEquals(displayedPersonCount, model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        InitCommand initT09 = new InitCommand(T09);
        InitCommand initT09Copy = new InitCommand(new Group("T09"));
        InitCommand initT10 = new InitCommand(new Group("T10"));

        assertTrue(initT09.equals(initT09));
        assertTrue(initT09.equals(initT09Copy));
        assertFalse(initT09.equals(initT10));
        assertFalse(initT09.equals(null));
    }
}
