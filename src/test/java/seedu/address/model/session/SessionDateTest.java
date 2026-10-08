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
    public void isValidDate() {
        assertFalse(SessionDate.isValidDate(null));
        assertFalse(SessionDate.isValidDate("2026-2-03"));
        assertFalse(SessionDate.isValidDate("2026-02-30"));
        assertTrue(SessionDate.isValidDate("2026-09-15"));
        assertTrue(SessionDate.isValidDate(" 2024-02-29 "));
    }

    @Test
    public void toString_validDate_returnsIsoDate() {
        assertEquals("2026-09-15", new SessionDate("2026-09-15").toString());
    }
}
