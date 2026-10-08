package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.session.Session;

/**
 * Creates a tutorial session for a registered group and teaching week.
 */
public class SessionCommand extends Command {

    public static final String COMMAND_WORD = "session";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Creates a tutorial session. "
            + "Parameters: " + PREFIX_GROUP + "GROUP " + PREFIX_WEEK + "WEEK " + PREFIX_DATE + "DATE\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_GROUP + "T09 " + PREFIX_WEEK + "5 "
            + PREFIX_DATE + "2026-09-15";

    public static final String MESSAGE_SUCCESS = "Created session: %1$s week %2$s (%3$s)";
    public static final String MESSAGE_GROUP_NOT_FOUND =
            "Tutorial group %1$s does not exist. Create it first with: init grp/%1$s";
    public static final String MESSAGE_DUPLICATE_SESSION =
            "%1$s already has a session for week %2$s (%3$s).";

    private final Session session;

    /**
     * Creates a {@code SessionCommand} for {@code session}.
     */
    public SessionCommand(Session session) {
        this.session = requireNonNull(session);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = session.getGroup();

        if (!model.hasGroup(group)) {
            throw new CommandException(String.format(MESSAGE_GROUP_NOT_FOUND, group));
        }

        Session existingSession = model.findSession(group, session.getWeek()).orElse(null);
        if (existingSession != null) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_SESSION, group,
                    existingSession.getWeek(), existingSession.getDate()));
        }

        model.addSession(session);
        return new CommandResult(String.format(MESSAGE_SUCCESS, group, session.getWeek(), session.getDate()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof SessionCommand otherCommand && session.equals(otherCommand.session));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("session", session).toString();
    }
}
