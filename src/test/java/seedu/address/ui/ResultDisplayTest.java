package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

public class ResultDisplayTest {

    @Test
    public void constructor_beforeAnyCommand_showsReadyPrompt() throws Exception {
        Region banner = onFxThread(() -> new ResultDisplay().getRoot());

        assertEquals("Ready", title(banner));
        assertEquals("Enter a command above to update the roster.", message(banner));
        assertFalse(banner.getStyleClass().contains("result-success"));
        assertFalse(banner.getStyleClass().contains("result-error"));
    }

    @Test
    public void setSuccessFeedback_showsCompletedBanner() throws Exception {
        Region banner = onFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.setSuccessFeedback("Listed all 3 students.");
            return display.getRoot();
        });

        assertEquals("Command completed", title(banner));
        assertEquals("✓", icon(banner));
        assertEquals("Listed all 3 students.", message(banner));
        assertTrue(banner.getStyleClass().contains("result-success"));
        assertFalse(banner.getStyleClass().contains("result-error"));
    }

    @Test
    public void setErrorFeedback_afterSuccess_replacesSuccessStyle() throws Exception {
        Region banner = onFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.setSuccessFeedback("Listed all 3 students.");
            display.setErrorFeedback("The student index provided is invalid.");
            return display.getRoot();
        });

        assertEquals("Command not run", title(banner));
        assertEquals("!", icon(banner));
        assertEquals("The student index provided is invalid.", message(banner));
        assertTrue(banner.getStyleClass().contains("result-error"));
        assertFalse(banner.getStyleClass().contains("result-success"));
    }

    @Test
    public void setFeedbackToUser_showsSuccessBanner() throws Exception {
        Region banner = onFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.setErrorFeedback("Unknown command");
            display.setFeedbackToUser("Roster has been cleared!");
            return display.getRoot();
        });

        assertEquals("Command completed", title(banner));
        assertTrue(banner.getStyleClass().contains("result-success"));
        assertFalse(banner.getStyleClass().contains("result-error"));
    }

    @Test
    public void setFeedback_null_throwsNullPointerException() throws Exception {
        ResultDisplay display = onFxThread(ResultDisplay::new);
        assertThrows(NullPointerException.class, () -> display.setSuccessFeedback(null));
        assertThrows(NullPointerException.class, () -> display.setErrorFeedback(null));
    }

    private static String title(Region banner) {
        return ((Label) banner.lookup("#resultTitle")).getText();
    }

    private static String icon(Region banner) {
        return ((Label) banner.lookup("#resultIcon")).getText();
    }

    private static String message(Region banner) {
        return ((TextArea) banner.lookup("#resultDisplay")).getText();
    }
}
