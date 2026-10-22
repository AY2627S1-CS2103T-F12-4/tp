package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Roster has been cleared!";

    private static final Logger logger = LogsCenter.getLogger(ClearCommand.class);

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        ReadOnlyAddressBook cleared = model.getAddressBook();
        // Everything is deleted at once, so the log records how much was lost
        logger.info("Clearing Roster, which had " + cleared.getPersonList().size() + " student(s), "
                + cleared.getGroupList().size() + " group(s) and " + cleared.getSessionList().size()
                + " session(s)");
        model.setAddressBook(new AddressBook());
        assert model.getAddressBook().getPersonList().isEmpty() && model.getAddressBook().getGroupList().isEmpty()
                : "Roster should be empty after clearing";
        return new CommandResult(MESSAGE_SUCCESS);
    }

}
