package seedu.address.testutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

/**
 * A utility class containing a list of {@code Session} objects to be used in tests.
 * The groups match those of {@link TypicalPersons}: ALICE to DANIEL are in T09, the rest in T10.
 */
public class TypicalSessions {

    public static final Session T09_WEEK_1 = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
    public static final Session T09_WEEK_2 = new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18"));
    public static final Session T10_WEEK_1 = new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-13"));

    // Manually added: not in the typical address book
    public static final Session T10_WEEK_2 = new Session(new Group("T10"), new Week(2), new SessionDate("2026-08-20"));

    private TypicalSessions() {} // prevents instantiation

    public static List<Session> getTypicalSessions() {
        return new ArrayList<>(Arrays.asList(T09_WEEK_1, T09_WEEK_2, T10_WEEK_1));
    }
}
