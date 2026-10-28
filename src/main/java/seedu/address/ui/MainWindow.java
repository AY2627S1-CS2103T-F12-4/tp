package seedu.address.ui;

import java.awt.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    /** Share of the window the student detail panel covers while it is open. */
    private static final double DETAIL_PANEL_SHARE = 0.45;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private GroupListPanel groupListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;
    private StudentDetailPanel studentDetailPanel;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane groupListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private StackPane studentDetailPlaceholder;

    @FXML
    private StackPane pageArea;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        groupListPanel = new GroupListPanel(logic.getGroupList(), logic.activeGroupProperty(),
                logic.showingAllStudentsProperty(), this::navigateToGroup);
        groupListPanelPlaceholder.getChildren().add(groupListPanel.getRoot());
        setGroupNavigationKeys();

        personListPanel = new PersonListPanel(logic.getFilteredPersonList(), logic.getSessionList(),
                logic.activeGroupProperty());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        studentDetailPanel = new StudentDetailPanel(this::hideStudentDetail);
        studentDetailPlaceholder.getChildren().add(studentDetailPanel.getRoot());
        personListPanel.setOnStudentChosen(this::showStudentDetail);
        sizeStudentDetailPanel();

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand, logic.getGroupList());
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        setUndoAccelerator();
        setDetailPanelEscape();
    }

    /**
     * Keeps the detail panel to a share of the window rather than a fixed width, so the student
     * list stays readable beside it even at the window's smallest size.
     */
    private void sizeStudentDetailPanel() {
        Region panel = studentDetailPanel.getRoot();
        panel.maxWidthProperty().bind(pageArea.widthProperty().multiply(DETAIL_PANEL_SHARE));
        panel.prefWidthProperty().bind(panel.maxWidthProperty());
        // The panel runs the page's full height, so it covers the command box while it is open
        panel.setMaxHeight(Double.MAX_VALUE);
    }

    /** Opens the panel on a student without disturbing the displayed list or the active group. */
    private void showStudentDetail(Person person, int displayedIndex) {
        studentDetailPanel.show(person, displayedIndex, logic.getSessionList());
    }

    private void hideStudentDetail() {
        studentDetailPanel.hide();
    }

    /**
     * Keeps an open panel showing the current version of its student after a command.
     * The student is found again by matriculation number, which identifies them, so an edit or a
     * new comment is reflected; the panel closes if they are no longer displayed.
     * {@code changed} is the student the command reported, which covers the one case the
     * matriculation number cannot: an edit that changed the number being searched for.
     */
    private void refreshStudentDetail(Person changed) {
        if (!studentDetailPanel.isShowing()) {
            return;
        }
        String shown = studentDetailPanel.shownStudent()
                .map(person -> person.getMatric().value).orElse(null);
        List<Person> displayed = logic.getFilteredPersonList();
        for (int i = 0; i < displayed.size(); i++) {
            if (displayed.get(i).getMatric().value.equals(shown)) {
                showStudentDetail(displayed.get(i), i + 1);
                return;
            }
        }
        // The open student's number no longer matches anyone, so an edit changed it. Only the
        // command knows who it touched, and only a displayed student can be shown.
        if (changed != null) {
            int index = displayed.indexOf(changed);
            if (index >= 0) {
                showStudentDetail(changed, index + 1);
                return;
            }
        }
        hideStudentDetail();
    }

    /**
     * Closes the detail panel on Escape.
     * This is a filter because a focused list consumes Escape itself, so a handler would never see
     * the key. The command box keeps Escape while its completion popup is open.
     */
    private void setDetailPanelEscape() {
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.ESCAPE || commandBox.isShowingCompletions()) {
                return;
            }
            if (studentDetailPanel.isShowing()) {
                hideStudentDetail();
                event.consume();
            }
        });
    }

    /**
     * Keeps the existing up/down shortcuts available in the horizontal group strip.
     */
    private void setGroupNavigationKeys() {
        groupListPanel.getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN) {
                return;
            }
            KeyCode horizontalKey = event.getCode() == KeyCode.UP ? KeyCode.LEFT : KeyCode.RIGHT;
            Event.fireEvent(event.getTarget(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", horizontalKey,
                    event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown()));
            event.consume();
        });
    }

    /**
     * Uses the platform undo shortcut for Roster data unless the command box has text to undo.
     */
    private void setUndoAccelerator() {
        KeyCombination undoShortcut = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!undoShortcut.match(event) || commandBox.hasUndoableTextEdit()) {
                return;
            }
            try {
                executeCommand("undo");
            } catch (CommandException | ParseException e) {
                // executeCommand has already shown the failure in the result display.
            }
            event.consume();
        });
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        Point coordinates = guiSettings.getWindowCoordinates();
        if (coordinates == null) {
            return;
        }
        Rectangle2D savedBounds = new Rectangle2D(coordinates.getX(), coordinates.getY(),
                guiSettings.getWindowWidth(), guiSettings.getWindowHeight());
        List<Rectangle2D> screens = Screen.getScreens().stream().map(Screen::getVisualBounds).toList();
        if (WindowPlacement.isReachable(savedBounds, screens)) {
            primaryStage.setX(coordinates.getX());
            primaryStage.setY(coordinates.getY());
        } else {
            // The window was last on a screen that is no longer connected, so it would open out of reach
            logger.info("Saved window position " + coordinates.getX() + ", " + coordinates.getY()
                    + " is not on any connected screen, so the window is centred instead");
            primaryStage.centerOnScreen();
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /** Runs navigation through the usual command path without touching unfinished command input. */
    private void navigateToGroup(String commandText) {
        try {
            executeCommand(commandText);
        } catch (CommandException | ParseException e) {
            // executeCommand has already displayed and logged the error.
        }
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setSuccessFeedback(commandResult.getFeedbackToUser());
            commandBox.setStyleToIndicateCommandSuccess();
            // A highlight is only kept until the next command, so it never marks an unrelated student
            commandResult.getPersonToShow()
                    .ifPresentOrElse(personListPanel::scrollTo, personListPanel::clearSelection);
            refreshStudentDetail(commandResult.getPersonToShow().orElse(null));

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorFeedback(e.getMessage());
            commandBox.setStyleToIndicateCommandFailure();
            personListPanel.clearSelection();
            throw e;
        }
    }
}
