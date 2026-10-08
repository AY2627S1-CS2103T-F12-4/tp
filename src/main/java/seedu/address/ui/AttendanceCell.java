package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;

import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * One teaching week's attendance in a student's displayed attendance strip.
 */
public record AttendanceCell(int week, String status) {

    /**
     * Returns a cell for every session of the student's group, in teaching-week order.
     * Missing attendance is displayed as an em dash, not as an absence.
     */
    public static List<AttendanceCell> forStudent(Person person, List<Session> sessions) {
        requireAllNonNull(person, sessions);
        return sessions.stream()
                .filter(session -> session.getGroup().equals(person.getGroup()))
                .sorted(Comparator.comparingInt(session -> session.getWeek().value))
                .map(session -> new AttendanceCell(session.getWeek().value, statusFor(person, session)))
                .toList();
    }

    private static String statusFor(Person person, Session session) {
        return person.getAttendances().stream()
                .filter(attendance -> attendance.isForSession(session))
                .map(attendance -> attendance.getStatus() == Status.PRESENT ? "P" : "A")
                .findFirst().orElse("\u2014");
    }
}
