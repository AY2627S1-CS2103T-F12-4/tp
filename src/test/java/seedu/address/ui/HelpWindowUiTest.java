package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import org.junit.jupiter.api.Test;

import javafx.stage.Stage;

/**
 * Tests how the help window responds to being asked for again while it is already open.
 * <p>
 * These tests work on the stage's own state rather than on a window the operating system has
 * drawn. Restoring and raising a window are window-manager operations, and the build runs on a
 * virtual display with no window manager, so asserting on what the screen shows would prove
 * nothing there. What is worth pinning is the contract {@code focus()} has to meet: whatever
 * state the window was left in, asking for it again must leave it no longer minimised.
 */
public class HelpWindowUiTest {

    @Test
    public void isShowing_minimisedWindow_staysTrue() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage());
            helpWindow.show();
            try {
                helpWindow.getRoot().setIconified(true);

                // A minimised stage still counts as showing. Reading that as "the window is in
                // front of the user" is what stopped the help window from ever coming back.
                assertTrue(helpWindow.isShowing());
            } finally {
                helpWindow.hide();
            }
            return null;
        });
    }

    @Test
    public void focus_minimisedWindow_clearsTheMinimisedState() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage());
            Stage stage = helpWindow.getRoot();
            stage.setIconified(true);
            assertTrue(stage.isIconified());

            helpWindow.focus();

            // Requesting focus alone left this true, which is why nothing happened on screen
            assertFalse(stage.isIconified());
            return null;
        });
    }

    @Test
    public void focus_windowNotMinimised_leavesItAlone() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage());

            helpWindow.focus();

            assertFalse(helpWindow.getRoot().isIconified());
            return null;
        });
    }
}
