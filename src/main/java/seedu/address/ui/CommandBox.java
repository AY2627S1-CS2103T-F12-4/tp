package seedu.address.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.FieldCompletion;
import seedu.address.logic.parser.FieldCompletion.Suggestion;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final ObservableList<Group> groups;
    private final Popup completionPopup = new Popup();
    private final ListView<Suggestion> suggestions = new ListView<>();
    private boolean acceptingCompletion;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor, ObservableList<Group> groups) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        this.groups = groups;
        suggestions.setId("fieldCompletionList");
        suggestions.getStylesheets().add(getClass().getResource("/view/Completion.css").toExternalForm());
        suggestions.setFixedCellSize(28);
        suggestions.setPrefWidth(260);
        suggestions.setFocusTraversable(false);
        suggestions.setOnMouseClicked(event -> {
            Node target = (Node) event.getTarget();
            while (target != null && !(target instanceof ListCell<?>)) {
                target = target.getParent();
            }
            if (target instanceof ListCell<?> cell && !cell.isEmpty()) {
                suggestions.getSelectionModel().select(cell.getIndex());
                acceptCompletion();
            }
        });
        completionPopup.getContent().add(suggestions);
        completionPopup.setAutoHide(true);
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleCompletionKey);
        commandTextField.caretPositionProperty().addListener((observable, oldValue, newValue) -> updateSuggestions());
        groups.addListener((ListChangeListener<Group>) change -> updateSuggestions());
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            updateSuggestions();
        });
    }

    /** Refreshes suggestions from the current text, caret and registered groups. */
    private void updateSuggestions() {
        if (acceptingCompletion || !commandTextField.isFocused()) {
            return;
        }
        suggestions.getItems().setAll(FieldCompletion.suggest(commandTextField.getText(),
                Math.min(commandTextField.getCaretPosition(), commandTextField.getLength()),
                groups.stream().map(Group::toString).toList()));
        if (suggestions.getItems().isEmpty()) {
            completionPopup.hide();
            return;
        }
        suggestions.getSelectionModel().selectFirst();
        suggestions.setPrefHeight(Math.min(6, suggestions.getItems().size()) * 28 + 2);
        Point2D position = commandTextField.localToScreen(0, commandTextField.getHeight());
        if (position != null) {
            completionPopup.show(commandTextField, position.getX(), position.getY());
        }
    }

    /** Keeps completion navigation separate from command execution. */
    private void handleCompletionKey(KeyEvent event) {
        if (!completionPopup.isShowing()) {
            return;
        }
        switch (event.getCode()) {
            case UP, DOWN:
                int offset = event.getCode() == javafx.scene.input.KeyCode.DOWN ? 1 : -1;
                int index = Math.floorMod(suggestions.getSelectionModel().getSelectedIndex() + offset,
                        suggestions.getItems().size());
                suggestions.getSelectionModel().select(index);
                suggestions.scrollTo(index);
                break;
            case TAB:
                acceptCompletion();
                break;
            case ESCAPE:
                completionPopup.hide();
                break;
            case ENTER:
                completionPopup.hide();
                return;
            default:
                return;
        }
        event.consume();
    }

    /** Accepts the highlighted replacement without executing the command. */
    private void acceptCompletion() {
        Suggestion selected = suggestions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            acceptingCompletion = true;
            commandTextField.setText(selected.apply(commandTextField.getText()));
            commandTextField.positionCaret(selected.start() + selected.value().length());
            acceptingCompletion = false;
            completionPopup.hide();
            commandTextField.requestFocus();
        }
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
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
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
