package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedSession.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Group;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class JsonAdaptedSessionTest {
    private static final String VALID_GROUP = T09_WEEK_1.getGroup().toString();
    private static final String VALID_WEEK = T09_WEEK_1.getWeek().toString();
    private static final String VALID_DATE = T09_WEEK_1.getDate().toString();

    @Test
    public void toModelType_validSessionDetails_returnsSession() throws Exception {
        JsonAdaptedSession session = new JsonAdaptedSession(T09_WEEK_1);
        assertEquals(T09_WEEK_1, session.toModelType());
    }

    @Test
    public void toModelType_invalidGroup_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession("T9", VALID_WEEK, VALID_DATE);
        assertThrows(IllegalValueException.class, Group.MESSAGE_CONSTRAINTS, session::toModelType);
    }

    @Test
    public void toModelType_nullGroup_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession(null, VALID_WEEK, VALID_DATE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Group.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, session::toModelType);
    }

    @Test
    public void toModelType_invalidWeek_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession(VALID_GROUP, "14", VALID_DATE);
        assertThrows(IllegalValueException.class, Week.MESSAGE_CONSTRAINTS, session::toModelType);
    }

    @Test
    public void toModelType_nullWeek_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession(VALID_GROUP, null, VALID_DATE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Week.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, session::toModelType);
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession(VALID_GROUP, VALID_WEEK, "2026-02-30");
        assertThrows(IllegalValueException.class, SessionDate.MESSAGE_CONSTRAINTS, session::toModelType);
    }

    @Test
    public void toModelType_nullDate_throwsIllegalValueException() {
        JsonAdaptedSession session = new JsonAdaptedSession(VALID_GROUP, VALID_WEEK, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, SessionDate.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, session::toModelType);
    }
}
