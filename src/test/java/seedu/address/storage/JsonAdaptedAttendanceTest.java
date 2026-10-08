package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedAttendance.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

public class JsonAdaptedAttendanceTest {
    private static final JsonAdaptedSession VALID_SESSION = new JsonAdaptedSession(T09_WEEK_1);

    @Test
    public void toModelType_validAttendanceDetails_returnsAttendance() throws Exception {
        Attendance attendance = new Attendance(T09_WEEK_1, Status.ABSENT);
        assertEquals(attendance, new JsonAdaptedAttendance(attendance).toModelType());
    }

    @Test
    public void toModelType_invalidSession_throwsIllegalValueException() {
        JsonAdaptedSession invalidSession = new JsonAdaptedSession("T09", "0", "2026-08-11");
        JsonAdaptedAttendance attendance = new JsonAdaptedAttendance(invalidSession, "PRESENT");
        assertThrows(IllegalValueException.class, Week.MESSAGE_CONSTRAINTS, attendance::toModelType);
    }

    @Test
    public void toModelType_nullSession_throwsIllegalValueException() {
        JsonAdaptedAttendance attendance = new JsonAdaptedAttendance(null, "PRESENT");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Session.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, attendance::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        JsonAdaptedAttendance attendance = new JsonAdaptedAttendance(VALID_SESSION, "LATE");
        assertThrows(IllegalValueException.class, Status.MESSAGE_CONSTRAINTS, attendance::toModelType);
    }

    @Test
    public void toModelType_nullStatus_throwsIllegalValueException() {
        JsonAdaptedAttendance attendance = new JsonAdaptedAttendance(VALID_SESSION, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Status.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, attendance::toModelType);
    }
}
