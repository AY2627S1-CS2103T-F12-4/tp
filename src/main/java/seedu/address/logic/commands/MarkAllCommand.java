package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

/**
 * Marks every student in the displayed list as present or absent for the session of their own group.
 */
public class MarkAllCommand extends Command {

    public static final String MESSAGE_SUCCESS = "Marked %1$d student(s) as %2$s for week %3$s in %4$s.";
    public static final String MESSAGE_SUCCESS_WITH_REMARKS =
            "Marked %1$d student(s) as %2$s for week %3$s in %4$s (%5$d already marked).";
    public static final String MESSAGE_NO_STUDENTS = "There are no students in the displayed list to mark.";

    private final Week week;
    private final Status status;

    /**
     * Creates a {@code MarkAllCommand} that marks every displayed student with {@code status} for {@code week}.
     */
    public MarkAllCommand(Week week, Status status) {
        requireAllNonNull(week, status);
        this.week = week;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> studentsToMark = List.copyOf(model.getFilteredPersonList());
        if (studentsToMark.isEmpty()) {
            throw new CommandException(MESSAGE_NO_STUDENTS);
        }

        // Every group's session is found first, so that no one is marked unless everyone can be
        Map<Group, Session> sessionsByGroup = new TreeMap<>(Comparator.comparing(group -> group.value));
        for (Person student : studentsToMark) {
            Group group = student.getGroup();
            if (!sessionsByGroup.containsKey(group)) {
                Session session = model.findSession(group, week).orElseThrow(() -> new CommandException(
                        String.format(MarkCommand.MESSAGE_NO_SESSION, group, week)));
                sessionsByGroup.put(group, session);
            }
        }

        int remarkCount = 0;
        for (Person student : studentsToMark) {
            Session session = sessionsByGroup.get(student.getGroup());
            if (student.getAttendance(session).isPresent()) {
                remarkCount++;
            }
            model.setPerson(student, student.withAttendance(new Attendance(session, status)));
        }

        String groups = sessionsByGroup.keySet().stream().map(Group::toString).collect(Collectors.joining(", "));
        return new CommandResult(remarkCount == 0
                ? String.format(MESSAGE_SUCCESS, studentsToMark.size(), status, week, groups)
                : String.format(MESSAGE_SUCCESS_WITH_REMARKS, studentsToMark.size(), status, week, groups,
                        remarkCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MarkAllCommand otherMarkAllCommand)) {
            return false;
        }

        return week.equals(otherMarkAllCommand.week)
                && status == otherMarkAllCommand.status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("week", week)
                .add("status", status)
                .toString();
    }
}
