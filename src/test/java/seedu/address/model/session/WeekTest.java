package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void isValidWeek_number() {
        assertFalse(Week.isValidWeek(0)); // below the first week
        assertFalse(Week.isValidWeek(-1)); // negative
        assertFalse(Week.isValidWeek(14)); // after the last week

        assertTrue(Week.isValidWeek(1)); // first week
        assertTrue(Week.isValidWeek(13)); // last week
    }

    @Test
    public void isValidWeek_string() {
        // null week
        assertThrows(NullPointerException.class, () -> Week.isValidWeek(null));

        // invalid weeks
        assertFalse(Week.isValidWeek("")); // empty string
        assertFalse(Week.isValidWeek(" ")); // spaces only
        assertFalse(Week.isValidWeek("0")); // below the first week
        assertFalse(Week.isValidWeek("14")); // after the last week
        assertFalse(Week.isValidWeek("05")); // leading zero
        assertFalse(Week.isValidWeek("-1")); // negative
        assertFalse(Week.isValidWeek("1.5")); // not a whole number
        assertFalse(Week.isValidWeek("five")); // not digits
        assertFalse(Week.isValidWeek("100")); // too many digits

        // valid weeks
        assertTrue(Week.isValidWeek("1"));
        assertTrue(Week.isValidWeek("5"));
        assertTrue(Week.isValidWeek("13"));
    }

    @Test
    public void toStringMethod() {
        assertEquals("5", new Week(5).toString());
    }

    @Test
    public void equals() {
        Week week = new Week(5);

        // same values -> returns true
        assertTrue(week.equals(new Week(5)));

        // same object -> returns true
        assertTrue(week.equals(week));

        // null -> returns false
        assertFalse(week.equals(null));

        // different types -> returns false
        assertFalse(week.equals(5));

        // different values -> returns false
        assertFalse(week.equals(new Week(6)));
    }
}
