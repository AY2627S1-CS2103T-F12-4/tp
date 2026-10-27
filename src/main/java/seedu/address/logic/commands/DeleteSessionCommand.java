package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

/** Deletes a session and every student's attendance for that session. */
public class DeleteSessionCommand extends Command {
    public static final String COMMAND_WORD = "deletesession";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a tutorial session and its attendance.\n"
            + "Parameters: grp/GROUP w/WEEK\n"
            + "Example: " + COMMAND_WORD + " grp/T09 w/5";
    public static final String MESSAGE_SUCCESS = "Deleted session: %1$s week %2$s (%3$s). "
            + "Attendance records for this session were removed. Use undo to restore them.";

    private static final Logger logger = LogsCenter.getLogger(DeleteSessionCommand.class);
    private final Group group;
    private final Week week;

    /** Identifies the session by its tutorial group and teaching week. */
    public DeleteSessionCommand(Group group, Week week) {
        requireAllNonNull(group, week);
        this.group = group;
        this.week = week;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Session session = model.findSession(group, week)
                .orElseThrow(() -> new CommandException(String.format(SessionCommand.MESSAGE_SESSION_NOT_FOUND,
                        group, week)));
        model.deleteSession(session);
        logger.fine("Deleted session and associated attendance for " + group + " week " + week);
        return new CommandResult(String.format(MESSAGE_SUCCESS, group, week, session.getDate()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteSessionCommand otherCommand
                && group.equals(otherCommand.group) && week.equals(otherCommand.week);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("group", group).add("week", week).toString();
    }
}
