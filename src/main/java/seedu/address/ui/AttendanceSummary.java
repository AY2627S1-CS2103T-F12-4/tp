package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * How a student's attendance stands across the sessions of their own group.
 *
 * @param present  sessions the student attended.
 * @param absent   sessions the student missed.
 * @param unmarked sessions of their group that nobody has recorded for them yet.
 */
public record AttendanceSummary(int present, int absent, int unmarked) {

    /**
     * Summarises {@code person}'s attendance over the sessions of their group.
     * Sessions of other groups are ignored, the way the attendance strip ignores them.
     */
    public static AttendanceSummary of(Person person, List<Session> sessions) {
        requireAllNonNull(person, sessions);
        int present = 0;
        int absent = 0;
        int unmarked = 0;
        for (Session session : sessions) {
            if (!session.getGroup().equals(person.getGroup())) {
                continue;
            }
            Status status = person.getAttendance(session).map(Attendance::getStatus).orElse(null);
            if (status == Status.PRESENT) {
                present++;
            } else if (status == Status.ABSENT) {
                absent++;
            } else {
                unmarked++;
            }
        }
        return new AttendanceSummary(present, absent, unmarked);
    }

    /** Returns how many sessions of the student's group exist. */
    public int total() {
        return present + absent + unmarked;
    }

    /** Returns how many of those sessions have been recorded either way. */
    public int marked() {
        return present + absent;
    }

    /**
     * Returns the share of marked sessions the student attended, from 0.0 to 1.0.
     * Unmarked sessions are left out rather than counted against the student, so the figure does
     * not fall simply because a tutor has not caught up with their marking. A student with nothing
     * marked yet scores 0.0, which {@link #marked()} distinguishes from genuinely never attending.
     */
    public double attendanceRate() {
        return marked() == 0 ? 0.0 : (double) present / marked();
    }
}
