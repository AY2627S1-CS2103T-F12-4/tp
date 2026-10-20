package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    public static final String SUCCESS_STYLE_CLASS = "success";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;

    @FXML
    private TextField commandTextField;
    @FXML
    private HBox commandBox;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
            setStyleToIndicateCommandSuccess();
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().removeAll(ERROR_STYLE_CLASS, SUCCESS_STYLE_CLASS);
        commandBox.getStyleClass().removeAll(ERROR_STYLE_CLASS, SUCCESS_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
        commandBox.getStyleClass().remove(SUCCESS_STYLE_CLASS);
        if (!commandBox.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            commandBox.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    /** Sets the command box accent after a command completes successfully. */
    void setStyleToIndicateCommandSuccess() {
        commandBox.getStyleClass().remove(ERROR_STYLE_CLASS);
        if (!commandBox.getStyleClass().contains(SUCCESS_STYLE_CLASS)) {
            commandBox.getStyleClass().add(SUCCESS_STYLE_CLASS);
        }
    }

    /** Returns whether the focused command box has a text edit that should receive the undo shortcut. */
    boolean hasUndoableTextEdit() {
        return commandTextField.isFocused() && commandTextField.isUndoable();
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
