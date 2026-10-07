package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SessionDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SessionDate(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SessionDate("2026-02-30"));
    }

    @Test
    public void isValidSessionDate() {
        // null date
        assertThrows(NullPointerException.class, () -> SessionDate.isValidSessionDate(null));

        // invalid dates
        assertFalse(SessionDate.isValidSessionDate("")); // empty string
        assertFalse(SessionDate.isValidSessionDate("2026-02-30")); // day does not exist
        assertFalse(SessionDate.isValidSessionDate("2026-13-01")); // month does not exist
        assertFalse(SessionDate.isValidSessionDate("2026-9-15")); // month not zero-padded
        assertFalse(SessionDate.isValidSessionDate("15-09-2026")); // wrong order
        assertFalse(SessionDate.isValidSessionDate("2026/09/15")); // wrong separator
        assertFalse(SessionDate.isValidSessionDate(" 2026-09-15")); // leading space

        // valid dates
        assertTrue(SessionDate.isValidSessionDate("2026-09-15"));
        assertTrue(SessionDate.isValidSessionDate("2028-02-29")); // leap day
    }

    @Test
    public void toStringMethod() {
        assertEquals("2026-09-15", new SessionDate("2026-09-15").toString());
    }

    @Test
    public void equals() {
        SessionDate date = new SessionDate("2026-09-15");

        // same values -> returns true
        assertTrue(date.equals(new SessionDate("2026-09-15")));

        // same object -> returns true
        assertTrue(date.equals(date));

        // null -> returns false
        assertFalse(date.equals(null));

        // different types -> returns false
        assertFalse(date.equals("2026-09-15"));

        // different values -> returns false
        assertFalse(date.equals(new SessionDate("2026-09-22")));
    }
}
