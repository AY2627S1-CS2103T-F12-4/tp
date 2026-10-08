package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.group.Group;
import seedu.address.model.session.exceptions.DuplicateSessionException;

public class UniqueSessionListTest {

    private static final Group T09 = new Group("T09");
    private static final Session WEEK_FIVE = new Session(T09, new Week(5), new SessionDate("2026-09-15"));
    private static final Session WEEK_SIX = new Session(T09, new Week(6), new SessionDate("2026-09-22"));

    private final UniqueSessionList sessions = new UniqueSessionList();

    @Test
    public void add_duplicateGroupAndWeek_throwsDuplicateSessionException() {
        sessions.add(WEEK_FIVE);

        assertThrows(DuplicateSessionException.class, () ->
                sessions.add(new Session(T09, new Week(5), new SessionDate("2026-09-16"))));
    }

    @Test
    public void add_outOfOrder_ordersByTeachingWeek() {
        sessions.add(WEEK_SIX);
        sessions.add(WEEK_FIVE);

        assertEquals(List.of(WEEK_FIVE, WEEK_SIX), sessions.asUnmodifiableObservableList());
    }

    @Test
    public void find_existingSession_returnsSession() {
        sessions.add(WEEK_FIVE);

        assertTrue(sessions.find(T09, new Week(5)).isPresent());
    }
}
