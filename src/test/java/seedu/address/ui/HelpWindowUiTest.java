package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import org.junit.jupiter.api.Test;

import javafx.stage.Stage;

public class HelpWindowUiTest {

    @Test
    public void isShowing_minimisedWindow_staysTrue() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = shownHelpWindow();
            try {
                helpWindow.getRoot().setIconified(true);

                // A minimised stage still counts as showing. Reading this as "the window is in
                // front of the user" is what stopped the help window from ever coming back.
                assertTrue(helpWindow.isShowing());
            } finally {
                helpWindow.hide();
            }
            return null;
        });
    }

    @Test
    public void focus_minimisedWindow_restoresIt() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = shownHelpWindow();
            try {
                Stage stage = helpWindow.getRoot();
                stage.setIconified(true);
                // Some platforms, including a headless CI machine, ignore minimising outright.
                // There is nothing to restore there, so skip rather than fail for the wrong reason.
                assumeTrue(stage.isIconified(), "This platform does not minimise windows");

                helpWindow.focus();

                assertFalse(stage.isIconified());
                assertTrue(helpWindow.isShowing());
            } finally {
                helpWindow.hide();
            }
            return null;
        });
    }

    @Test
    public void focus_windowAlreadyInView_keepsItShowing() throws Exception {
        onFxThread(() -> {
            HelpWindow helpWindow = shownHelpWindow();
            try {
                helpWindow.focus();

                assertTrue(helpWindow.isShowing());
                assertFalse(helpWindow.getRoot().isIconified());
            } finally {
                helpWindow.hide();
            }
            return null;
        });
    }

    private static HelpWindow shownHelpWindow() {
        HelpWindow helpWindow = new HelpWindow(new Stage());
        helpWindow.show();
        return helpWindow;
    }
}
