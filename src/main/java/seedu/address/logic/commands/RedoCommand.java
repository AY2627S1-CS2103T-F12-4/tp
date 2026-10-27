package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Reapplies the most recently undone change. */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Reapplies the most recently undone command.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_NOTHING_TO_REDO = "There are no changes to redo.";
    public static final String MESSAGE_SUCCESS = "Redid the most recent %1$s command.";

    private static final Logger logger = LogsCenter.getLogger(RedoCommand.class);

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.canRedoAddressBook()) {
            throw new CommandException(MESSAGE_NOTHING_TO_REDO);
        }
        String redoneCommand = model.getRedoDescription();
        model.redoAddressBook();
        logger.fine("Redid the most recent " + redoneCommand + " command");
        return new CommandResult(String.format(MESSAGE_SUCCESS, redoneCommand));
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.NONE;
    }
}
