package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class UndoShortcutUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void shortcut_fromOutsideCommandBox_undoesRosterChange() throws Exception {
        Person original = new PersonBuilder().withName("Alex Tan").build();
        AddressBook book = new AddressBookBuilder().withPerson(original).build();
        ModelManager model = new ModelManager(book, new UserPrefs());
        Stage stage = createWindow(model);

        try {
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                input.setText("edit 1 n/Alex Lim");
                input.fireEvent(new ActionEvent());
                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);

                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                result.requestFocus();
                result.fireEvent(platformShortcutZ());

                assertEquals(original, model.getAddressBook().getPersonList().getFirst());
                assertEquals("Undid the most recent edit command.", result.getText());
                return null;
            });
        } finally {
            close(stage);
        }
    }

    @Test
    public void shortcut_withUndoableCommandBoxText_doesNotUndoRosterChange() throws Exception {
        Person original = new PersonBuilder().withName("Alex Tan").build();
        AddressBook book = new AddressBookBuilder().withPerson(original).build();
        ModelManager model = new ModelManager(book, new UserPrefs());
        Stage stage = createWindow(model);

        try {
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                input.setText("edit 1 n/Alex Lim");
                input.fireEvent(new ActionEvent());
                input.requestFocus();
                input.appendText("draft command");
                assertTrue(input.isFocused());
                assertTrue(input.isUndoable());
                input.fireEvent(platformShortcutZ());

                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);
                assertEquals("", input.getText());
                return null;
            });
        } finally {
            close(stage);
        }
    }

    @Test
    public void shortcut_afterTabCompletion_undoesTextAndPreservesRoster() throws Exception {
        assertCompletionUndoPreservesRoster(false);
    }

    @Test
    public void shortcut_afterMouseCompletion_undoesTextAndPreservesRoster() throws Exception {
        assertCompletionUndoPreservesRoster(true);
    }

    private void assertCompletionUndoPreservesRoster(boolean useMouse) throws Exception {
        Person original = new PersonBuilder().withName("Alex Tan").build();
        ModelManager model = new ModelManager(new AddressBookBuilder().withPerson(original).build(), new UserPrefs());
        Stage stage = createWindow(model);
        try {
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                input.setText("edit 1 n/Alex Lim");
                input.fireEvent(new ActionEvent());
                input.requestFocus();
                input.appendText("ad");
                assertTrue(input.isFocused());
                assertTrue(input.isUndoable());

                if (useMouse) {
                    Popup popup = (Popup) Window.getWindows().stream()
                            .filter(window -> window instanceof Popup completion
                                    && completion.getOwnerWindow() == stage)
                            .findFirst().orElseThrow();
                    ListView<?> options = (ListView<?>) popup.getContent().getFirst();
                    options.applyCss();
                    options.layout();
                    ListCell<?> cell = options.lookupAll(".list-cell").stream()
                            .map(node -> (ListCell<?>) node)
                            .filter(row -> row.getIndex() == 0 && !row.isEmpty()).findFirst().orElseThrow();
                    cell.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                            false, false, false, false, false, false, false, true, false, true, null));
                } else {
                    input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                            false, false, false, false));
                }
                assertEquals("add", input.getText());
                assertTrue(input.isUndoable());
                input.fireEvent(platformShortcutZ());

                assertEquals("ad", input.getText());
                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);
                assertTrue(input.isRedoable());
                input.redo();
                assertEquals("add", input.getText());
                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);
                return null;
            });
        } finally {
            close(stage);
        }
    }

    @Test
    public void shortcut_withoutHistory_showsErrorAndPreservesRoster() throws Exception {
        ModelManager model = new ModelManager();
        Stage stage = createWindow(model);
        try {
            onFxThread(() -> {
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                result.requestFocus();
                result.fireEvent(platformShortcutZ());
                assertEquals("There are no changes to undo.", result.getText());
                assertEquals(new AddressBook(), model.getAddressBook());
                return null;
            });
        } finally {
            close(stage);
        }
    }

    private Stage createWindow(ModelManager model) throws Exception {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        return onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    addressBookStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
    }

    private static KeyEvent platformShortcutZ() {
        boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.Z, false, !isMac, false, isMac);
    }

    private static void close(Stage stage) throws Exception {
        onFxThread(() -> {
            stage.close();
            return null;
        });
    }
}
