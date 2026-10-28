package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_STYLE_CLASS = "result-success";
    private static final String ERROR_STYLE_CLASS = "result-error";

    @FXML
    private HBox placeHolder;
    @FXML
    private Label resultIcon;
    @FXML
    private Label resultTitle;
    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setSuccessFeedback(feedbackToUser);
    }

    /** Shows feedback for a command that completed successfully. */
    public void setSuccessFeedback(String feedbackToUser) {
        setFeedback(feedbackToUser, false);
    }

    /** Shows feedback for a command that could not be completed. */
    public void setErrorFeedback(String feedbackToUser) {
        setFeedback(feedbackToUser, true);
    }

    private void setFeedback(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultTitle.setText(isError ? "Command not run" : "Command completed");
        resultIcon.setText(isError ? "!" : "\u2713");
        placeHolder.getStyleClass().removeAll(SUCCESS_STYLE_CLASS, ERROR_STYLE_CLASS);
        placeHolder.getStyleClass().add(isError ? ERROR_STYLE_CLASS : SUCCESS_STYLE_CLASS);
    }

}
