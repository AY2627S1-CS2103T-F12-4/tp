package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.session.Session;

/** Changes a session's date while preserving its group, week and attendance. */
public class EditSessionCommand extends Command {
    public static final String COMMAND_WORD = "editsession";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Changes a tutorial session's date.\n"
            + "Parameters: grp/GROUP w/WEEK d/DATE\n"
            + "Example: " + COMMAND_WORD + " grp/T09 w/5 d/2026-09-16";
    public static final String MESSAGE_SUCCESS =
            "Updated session: %1$s week %2$s (%3$s -> %4$s). Attendance is unchanged.";
    public static final String MESSAGE_NO_CHANGE = "The session already has date %1$s.";

    private static final Logger logger = LogsCenter.getLogger(EditSessionCommand.class);
    private final Session editedSession;

    /** Supplies the replacement date with the original group/week identity. */
    public EditSessionCommand(Session editedSession) {
        this.editedSession = requireNonNull(editedSession);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Session original = model.findSession(editedSession.getGroup(), editedSession.getWeek())
                .orElseThrow(() -> new CommandException(String.format(SessionCommand.MESSAGE_SESSION_NOT_FOUND,
                        editedSession.getGroup(), editedSession.getWeek())));
        if (original.equals(editedSession)) {
            throw new CommandException(String.format(MESSAGE_NO_CHANGE, editedSession.getDate()));
        }
        model.setSession(original, editedSession);
        logger.fine("Changed session date for " + editedSession.getGroup() + " week " + editedSession.getWeek());
        return new CommandResult(String.format(MESSAGE_SUCCESS, editedSession.getGroup(), editedSession.getWeek(),
                original.getDate(), editedSession.getDate()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditSessionCommand otherCommand
                && editedSession.equals(otherCommand.editedSession);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("editedSession", editedSession).toString();
    }
}
