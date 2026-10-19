package seedu.address.logic.commands;

import java.util.Locale;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /** Describes how a successful command changes the undo history. */
    public enum HistoryAction {
        NONE,
        COMMIT
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns whether successful execution requires saving the roster to disk.
     */
    public boolean requiresSave() {
        return true;
    }

    /** Returns how this command changes the undo history after it is saved successfully. */
    public HistoryAction getHistoryAction() {
        return requiresSave() ? HistoryAction.COMMIT : HistoryAction.NONE;
    }

    /** Returns the user-facing command name stored with an undoable state. */
    public String getUndoDescription() {
        return getClass().getSimpleName().replace("Command", "").toLowerCase(Locale.ROOT);
    }

}
