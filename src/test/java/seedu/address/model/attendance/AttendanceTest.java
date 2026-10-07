package seedu.address.model.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class AttendanceTest {

    private final Attendance presentWeek1 = new Attendance(T09_WEEK_1, Status.PRESENT);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Attendance(null, Status.PRESENT));
        assertThrows(NullPointerException.class, () -> new Attendance(T09_WEEK_1, null));
    }

    @Test
    public void isForSession() {
        // same session -> returns true
        assertTrue(presentWeek1.isForSession(T09_WEEK_1));

        // same group and week, different date -> returns true
        Session differentDate = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"));
        assertTrue(presentWeek1.isForSession(differentDate));

        // different session -> returns false
        assertFalse(presentWeek1.isForSession(T09_WEEK_2));
    }

    @Test
    public void equals() {
        // same values -> returns true
        assertTrue(presentWeek1.equals(new Attendance(T09_WEEK_1, Status.PRESENT)));

        // same object -> returns true
        assertTrue(presentWeek1.equals(presentWeek1));

        // null -> returns false
        assertFalse(presentWeek1.equals(null));

        // different type -> returns false
        assertFalse(presentWeek1.equals(5));

        // different status -> returns false
        assertFalse(presentWeek1.equals(new Attendance(T09_WEEK_1, Status.ABSENT)));

        // different session -> returns false
        assertFalse(presentWeek1.equals(new Attendance(T09_WEEK_2, Status.PRESENT)));
    }

    @Test
    public void toStringMethod() {
        String expected = Attendance.class.getCanonicalName() + "{session=" + T09_WEEK_1 + ", status=PRESENT}";
        assertEquals(expected, presentWeek1.toString());
    }
}
