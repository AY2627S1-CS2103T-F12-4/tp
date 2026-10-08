package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class WeekTest {

    @Test
    public void constructor_invalidWeek_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Week(0));
        assertThrows(IllegalArgumentException.class, () -> new Week(14));
    }

    @Test
    public void isValidWeek() {
        assertFalse(Week.isValidWeek(0));
        assertFalse(Week.isValidWeek(14));
        assertTrue(Week.isValidWeek(1));
        assertTrue(Week.isValidWeek(13));
        assertFalse(Week.isValidWeek("05"));
        assertTrue(Week.isValidWeek("5"));
    }
}
