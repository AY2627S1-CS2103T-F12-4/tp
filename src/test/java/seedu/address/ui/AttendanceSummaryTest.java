package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;

public class AttendanceSummaryTest {

    private static final double TOLERANCE = 0.0001;

    private static final Session T09_WEEK_1 =
            new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
    private static final Session T09_WEEK_2 =
            new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18"));
    private static final Session T09_WEEK_3 =
            new Session(new Group("T09"), new Week(3), new SessionDate("2026-08-25"));
    private static final Session T10_WEEK_1 =
            new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-12"));

    @Test
    public void of_mixedAttendance_countsEachStateSeparately() {
        Person student = new PersonBuilder().withGroup("T09")
                .withAttendance(T09_WEEK_1, Status.PRESENT)
                .withAttendance(T09_WEEK_2, Status.ABSENT)
                .build();

        // Week 3 exists for the group but has not been marked for this student
        AttendanceSummary summary = AttendanceSummary.of(student,
                List.of(T09_WEEK_1, T09_WEEK_2, T09_WEEK_3));

        assertEquals(new AttendanceSummary(1, 1, 1), summary);
        assertEquals(3, summary.total());
        assertEquals(2, summary.marked());
    }

    @Test
    public void of_sessionsOfOtherGroups_areIgnored() {
        Person student = new PersonBuilder().withGroup("T09")
                .withAttendance(T09_WEEK_1, Status.PRESENT).build();

        AttendanceSummary summary = AttendanceSummary.of(student, List.of(T09_WEEK_1, T10_WEEK_1));

        assertEquals(new AttendanceSummary(1, 0, 0), summary);
        assertEquals(1, summary.total());
    }

    @Test
    public void attendanceRate_countsOnlyMarkedSessions() {
        Person student = new PersonBuilder().withGroup("T09")
                .withAttendance(T09_WEEK_1, Status.PRESENT)
                .withAttendance(T09_WEEK_2, Status.PRESENT)
                .build();

        // An unmarked week 3 must not drag the rate down from 100%
        AttendanceSummary summary = AttendanceSummary.of(student,
                List.of(T09_WEEK_1, T09_WEEK_2, T09_WEEK_3));

        assertEquals(1.0, summary.attendanceRate(), TOLERANCE);
        assertEquals(3, summary.total());
        assertEquals(2, summary.marked());
    }

    @Test
    public void attendanceRate_halfAttended_isAHalf() {
        Person student = new PersonBuilder().withGroup("T09")
                .withAttendance(T09_WEEK_1, Status.PRESENT)
                .withAttendance(T09_WEEK_2, Status.ABSENT)
                .build();

        assertEquals(0.5, AttendanceSummary.of(student, List.of(T09_WEEK_1, T09_WEEK_2))
                .attendanceRate(), TOLERANCE);
    }

    @Test
    public void attendanceRate_nothingMarked_isZeroRatherThanDividingByZero() {
        Person student = new PersonBuilder().withGroup("T09").build();

        AttendanceSummary summary = AttendanceSummary.of(student, List.of(T09_WEEK_1, T09_WEEK_2));

        assertEquals(0.0, summary.attendanceRate(), TOLERANCE);
        assertEquals(0, summary.marked());
        assertEquals(2, summary.unmarked());
    }

    @Test
    public void of_noSessions_isAllZero() {
        AttendanceSummary summary = AttendanceSummary.of(new PersonBuilder().build(), List.of());

        assertEquals(new AttendanceSummary(0, 0, 0), summary);
        assertEquals(0, summary.total());
        assertEquals(0.0, summary.attendanceRate(), TOLERANCE);
    }

    @Test
    public void of_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AttendanceSummary.of(null, List.of()));
        assertThrows(NullPointerException.class, () ->
                AttendanceSummary.of(new PersonBuilder().build(), null));
    }
}
