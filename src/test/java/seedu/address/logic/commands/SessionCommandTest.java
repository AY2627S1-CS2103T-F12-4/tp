package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.group.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class SessionCommandTest {

    private static final Group T09 = new Group("T09");
    private static final Session WEEK_FIVE =
            new Session(T09, new Week(5), new SessionDate("2026-09-15"));

    @Test
    public void constructor_nullSession_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SessionCommand(null));
    }

    @Test
    public void execute_existingGroup_success() {
        Model model = new ModelManager();
        model.addGroup(T09);
        Model expectedModel = new ModelManager();
        expectedModel.addGroup(T09);
        expectedModel.addSession(WEEK_FIVE);

        assertCommandSuccess(new SessionCommand(WEEK_FIVE), model,
                String.format(SessionCommand.MESSAGE_SUCCESS, T09, WEEK_FIVE.getWeek(), WEEK_FIVE.getDate()),
                expectedModel);
    }

    @Test
    public void execute_missingGroup_failure() {
        Model model = new ModelManager();

        assertCommandFailure(new SessionCommand(WEEK_FIVE), model,
                String.format(SessionCommand.MESSAGE_GROUP_NOT_FOUND, T09));
    }

    @Test
    public void execute_duplicateGroupAndWeek_failure() {
        Model model = new ModelManager();
        model.addGroup(T09);
        model.addSession(WEEK_FIVE);
        Session duplicateWithDifferentDate =
                new Session(T09, new Week(5), new SessionDate("2026-09-16"));

        assertCommandFailure(new SessionCommand(duplicateWithDifferentDate), model,
                String.format(SessionCommand.MESSAGE_DUPLICATE_SESSION, T09,
                        WEEK_FIVE.getWeek(), WEEK_FIVE.getDate()));
    }

    @Test
    public void execute_sessionsCreatedOutOfOrder_sortedByWeek() throws Exception {
        Model model = new ModelManager();
        model.addGroup(T09);
        Session weekSix = new Session(T09, new Week(6), new SessionDate("2026-09-22"));

        new SessionCommand(weekSix).execute(model);
        new SessionCommand(WEEK_FIVE).execute(model);

        assertEquals(WEEK_FIVE, model.getSessionList().get(0));
        assertEquals(weekSix, model.getSessionList().get(1));
    }

    @Test
    public void equals() {
        SessionCommand weekFive = new SessionCommand(WEEK_FIVE);
        SessionCommand weekFiveCopy = new SessionCommand(
                new Session(new Group("t09"), new Week(5), new SessionDate("2026-09-15")));
        SessionCommand weekSix = new SessionCommand(
                new Session(T09, new Week(6), new SessionDate("2026-09-22")));

        assertTrue(weekFive.equals(weekFive));
        assertTrue(weekFive.equals(weekFiveCopy));
        assertFalse(weekFive.equals(weekSix));
        assertFalse(weekFive.equals(null));
    }
}
