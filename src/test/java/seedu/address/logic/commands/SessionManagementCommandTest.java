package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class SessionManagementCommandTest {
    private static final Session SESSION =
            new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));

    @Test
    public void constructors_missingSessionIdentity_rejectNull() {
        assertThrows(NullPointerException.class, () -> new EditSessionCommand(null));
        assertThrows(NullPointerException.class, () -> new DeleteSessionCommand(null, new Week(1)));
        assertThrows(NullPointerException.class, () -> new DeleteSessionCommand(new Group("T09"), null));
        assertThrows(NullPointerException.class, () -> new EditSessionCommand(SESSION).execute(null));
        DeleteSessionCommand delete = new DeleteSessionCommand(new Group("T09"), new Week(1));
        assertThrows(NullPointerException.class, () -> delete.execute(null));
    }

    @Test
    public void equals_editIncludesReplacementDate_deleteUsesGroupAndWeek() {
        EditSessionCommand edit = new EditSessionCommand(SESSION);
        DeleteSessionCommand delete = new DeleteSessionCommand(new Group("T09"), new Week(1));
        assertEquals(edit, edit);
        assertEquals(edit, new EditSessionCommand(SESSION));
        assertNotEquals(edit, new EditSessionCommand(
                new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-12"))));
        assertNotEquals(edit, delete);
        assertNotEquals(edit, null);
        assertEquals(delete, delete);
        assertEquals(delete, new DeleteSessionCommand(new Group("T09"), new Week(1)));
        assertNotEquals(delete, new DeleteSessionCommand(new Group("T10"), new Week(1)));
        assertNotEquals(delete, new DeleteSessionCommand(new Group("T09"), new Week(2)));
        assertNotEquals(delete, edit);
        assertNotEquals(delete, null);
    }
}
