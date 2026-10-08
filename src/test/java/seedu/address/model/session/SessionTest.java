package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;

public class SessionTest {

    private static final Group T09 = new Group("T09");
    private static final Week WEEK_FIVE = new Week(5);
    private static final Session SESSION = new Session(T09, WEEK_FIVE, new SessionDate("2026-09-15"));

    @Test
    public void isSameSession_sameGroupAndWeek_returnsTrue() {
        Session differentDate = new Session(T09, WEEK_FIVE, new SessionDate("2026-09-16"));

        assertTrue(SESSION.isSameSession(differentDate));
    }

    @Test
    public void isFor_matchingGroupAndWeek_returnsTrue() {
        assertTrue(SESSION.isFor(T09, WEEK_FIVE));
        assertFalse(SESSION.isFor(T09, new Week(6)));
    }
}
