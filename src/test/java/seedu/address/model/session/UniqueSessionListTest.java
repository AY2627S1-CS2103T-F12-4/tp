package seedu.address.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;
import seedu.address.model.session.exceptions.DuplicateSessionException;

public class UniqueSessionListTest {

    private final UniqueSessionList uniqueSessionList = new UniqueSessionList();

    @Test
    public void contains_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueSessionList.contains(null));
    }

    @Test
    public void contains_sessionNotInList_returnsFalse() {
        assertFalse(uniqueSessionList.contains(T09_WEEK_1));
    }

    @Test
    public void contains_sessionWithSameGroupAndWeekInList_returnsTrue() {
        uniqueSessionList.add(T09_WEEK_1);
        Session differentDate = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"));
        assertTrue(uniqueSessionList.contains(differentDate));
    }

    @Test
    public void add_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueSessionList.add(null));
    }

    @Test
    public void add_duplicateSession_throwsDuplicateSessionException() {
        uniqueSessionList.add(T09_WEEK_1);
        Session differentDate = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"));
        assertThrows(DuplicateSessionException.class, () -> uniqueSessionList.add(differentDate));
    }

    @Test
    public void find_sessionInList_returnsSession() {
        uniqueSessionList.add(T09_WEEK_1);
        uniqueSessionList.add(T10_WEEK_1);
        assertEquals(Optional.of(T10_WEEK_1), uniqueSessionList.find(new Group("T10"), new Week(1)));
    }

    @Test
    public void find_sessionNotInList_returnsEmpty() {
        uniqueSessionList.add(T09_WEEK_1);
        assertEquals(Optional.empty(), uniqueSessionList.find(new Group("T09"), new Week(2)));
        assertEquals(Optional.empty(), uniqueSessionList.find(new Group("T10"), new Week(1)));
    }

    @Test
    public void setSessions_list_replacesOwnListWithProvidedList() {
        uniqueSessionList.add(T09_WEEK_1);
        List<Session> sessionList = Arrays.asList(T09_WEEK_2, T10_WEEK_1);
        uniqueSessionList.setSessions(sessionList);
        UniqueSessionList expectedUniqueSessionList = new UniqueSessionList();
        expectedUniqueSessionList.add(T09_WEEK_2);
        expectedUniqueSessionList.add(T10_WEEK_1);
        assertEquals(expectedUniqueSessionList, uniqueSessionList);
    }

    @Test
    public void setSessions_listWithDuplicateSessions_throwsDuplicateSessionException() {
        List<Session> listWithDuplicateSessions = Arrays.asList(T09_WEEK_1, T09_WEEK_1);
        assertThrows(DuplicateSessionException.class, () -> uniqueSessionList.setSessions(listWithDuplicateSessions));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniqueSessionList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void equals() {
        uniqueSessionList.add(T09_WEEK_1);

        // same object -> returns true
        assertTrue(uniqueSessionList.equals(uniqueSessionList));

        // same sessions -> returns true
        UniqueSessionList otherList = new UniqueSessionList();
        otherList.add(T09_WEEK_1);
        assertTrue(uniqueSessionList.equals(otherList));

        // null -> returns false
        assertFalse(uniqueSessionList.equals(null));

        // different sessions -> returns false
        assertFalse(uniqueSessionList.equals(new UniqueSessionList()));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniqueSessionList.asUnmodifiableObservableList().toString(), uniqueSessionList.toString());
    }
}
