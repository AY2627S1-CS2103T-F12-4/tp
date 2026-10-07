package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

/**
 * Marks a student, identified using their displayed index, as present or absent for a session of their group.
 */
public class MarkCommand extends Command {

    public static final String COMMAND_WORD = "mark";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Marks a student's attendance for a session.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_WEEK + "WEEK "
            + PREFIX_STATUS + "STATUS\n"
            + "Example: " + COMMAND_WORD + " 3 "
            + PREFIX_WEEK + "5 "
            + PREFIX_STATUS + "present";

    public static final String MESSAGE_MARK_SUCCESS = "Marked %1$s as %2$s for %3$s week %4$s.";
    public static final String MESSAGE_REMARK_SUCCESS = "Marked %1$s as %2$s for %3$s week %4$s (was %5$s).";
    public static final String MESSAGE_NO_SESSION =
            "%1$s has no session for week %2$s. Create it first with: session grp/%1$s w/%2$s d/YYYY-MM-DD";

    private final Index index;
    private final Week week;
    private final Status status;

    /**
     * @param index of the student in the filtered student list to mark
     * @param week of the session to mark the student for
     * @param status to record for the student
     */
    public MarkCommand(Index index, Week week, Status status) {
        requireAllNonNull(index, week, status);
        this.index = index;
        this.week = week;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToMark = lastShownList.get(index.getZeroBased());
        Group group = personToMark.getGroup();
        // Each student's week is looked up in their own group, so a list spanning several groups still works
        Session session = model.findSession(group, week)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NO_SESSION, group, week)));

        Optional<Attendance> previousAttendance = personToMark.getAttendance(session);
        Person markedPerson = personToMark.withAttendance(new Attendance(session, status));

        // The displayed list is left as it is, so that the indexes the TA sees do not change
        model.setPerson(personToMark, markedPerson);

        return new CommandResult(generateSuccessMessage(markedPerson, previousAttendance));
    }

    /**
     * Generates the message for a successful mark, naming the student so that the TA can confirm the right row
     * was changed, and the previous status if an earlier mark was overwritten.
     */
    private String generateSuccessMessage(Person markedPerson, Optional<Attendance> previousAttendance) {
        String name = markedPerson.getName().fullName;
        Group group = markedPerson.getGroup();
        return previousAttendance
                .map(previous -> String.format(MESSAGE_REMARK_SUCCESS, name, status, group, week,
                        previous.getStatus()))
                .orElseGet(() -> String.format(MESSAGE_MARK_SUCCESS, name, status, group, week));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MarkCommand otherMarkCommand)) {
            return false;
        }

        return index.equals(otherMarkCommand.index)
                && week.equals(otherMarkCommand.week)
                && status == otherMarkCommand.status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("week", week)
                .add("status", status)
                .toString();
    }
}
