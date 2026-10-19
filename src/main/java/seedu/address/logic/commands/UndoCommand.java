package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Restores the state before the most recent undoable command. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Reverses the most recent undoable command.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_NOTHING_TO_UNDO = "There are no changes to undo.";
    public static final String MESSAGE_SUCCESS = "Undid the most recent %1$s command.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.canUndoAddressBook()) {
            throw new CommandException(MESSAGE_NOTHING_TO_UNDO);
        }
        String undoneCommand = model.undoAddressBook();
        return new CommandResult(String.format(MESSAGE_SUCCESS, undoneCommand));
    }
}
