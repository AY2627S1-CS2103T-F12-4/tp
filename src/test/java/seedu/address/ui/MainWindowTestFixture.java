package seedu.address.ui;

import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.FxTestUtil.waitForRenderingCycles;

import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** A real window and isolated storage shared by UI workflow tests. */
record MainWindowTestFixture(Stage stage, ModelManager model, JsonAddressBookStorage storage)
        implements AutoCloseable {

    /** Saves the initial roster and opens its window on the JavaFX application thread. */
    static MainWindowTestFixture create(AddressBook book, Path folder) throws Exception {
        MainWindowTestFixture fixture = onFxThread(() -> {
            ModelManager model = new ModelManager(book, new UserPrefs());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("roster.json"));
            storage.saveAddressBook(book);
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return new MainWindowTestFixture(window.getPrimaryStage(), model, storage);
        });
        // Allow layout and native focus changes after showing a new window.
        waitForRenderingCycles(2);
        return fixture;
    }

    /** Enters a command through the real field; call this on the JavaFX application thread. */
    static void enterCommand(Stage stage, String command) {
        getCommandInput(stage).setText(command);
        getCommandInput(stage).fireEvent(new ActionEvent());
    }

    static TextField getCommandInput(Stage stage) {
        return (TextField) stage.getScene().lookup("#commandTextField");
    }

    @Override
    public void close() throws Exception {
        onFxThread(() -> {
            stage.close();
            return null;
        });
    }
}
