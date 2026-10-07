package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;

public class SessionTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Session(null, new Week(1),
                new SessionDate("2026-08-11")));
    }

    @Test
    public void isSameSession() {
        // same object -> returns true
        assertTrue(T09_WEEK_1.isSameSession(T09_WEEK_1));

        // null -> returns false
        assertFalse(T09_WEEK_1.isSameSession(null));

        // same group and week, different date -> returns true
        Session differentDate = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"));
        assertTrue(T09_WEEK_1.isSameSession(differentDate));

        // different week -> returns false
        assertFalse(T09_WEEK_1.isSameSession(T09_WEEK_2));

        // different group -> returns false
        assertFalse(T09_WEEK_1.isSameSession(T10_WEEK_1));
    }

    @Test
    public void isFor() {
        assertTrue(T09_WEEK_1.isFor(new Group("T09"), new Week(1)));
        assertFalse(T09_WEEK_1.isFor(new Group("T09"), new Week(2)));
        assertFalse(T09_WEEK_1.isFor(new Group("T10"), new Week(1)));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Session copy = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        assertTrue(T09_WEEK_1.equals(copy));

        // same object -> returns true
        assertTrue(T09_WEEK_1.equals(T09_WEEK_1));

        // null -> returns false
        assertFalse(T09_WEEK_1.equals(null));

        // different type -> returns false
        assertFalse(T09_WEEK_1.equals(5));

        // different date -> returns false
        Session differentDate = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"));
        assertFalse(T09_WEEK_1.equals(differentDate));

        // different week -> returns false
        assertFalse(T09_WEEK_1.equals(T09_WEEK_2));

        // different group -> returns false
        assertFalse(T09_WEEK_1.equals(T10_WEEK_1));
    }

    @Test
    public void toStringMethod() {
        String expected = Session.class.getCanonicalName() + "{group=" + T09_WEEK_1.getGroup()
                + ", week=" + T09_WEEK_1.getWeek() + ", date=" + T09_WEEK_1.getDate() + "}";
        assertEquals(expected, T09_WEEK_1.toString());
    }
}
