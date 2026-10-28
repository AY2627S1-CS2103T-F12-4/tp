package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests that the main window only reopens at its saved position when that position is on a connected screen.
 */
public class MainWindowPlacementTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void constructor_savedPositionOnScreen_usesSavedPosition() throws Exception {
        Rectangle2D screen = onFxThread(() -> Screen.getPrimary().getVisualBounds());
        int x = (int) screen.getMinX() + 10;
        int y = (int) screen.getMinY() + 10;

        Rectangle2D window = openWindowAt(new GuiSettings(740, 600, x, y));

        assertEquals(x, window.getMinX());
        assertEquals(y, window.getMinY());
    }

    @Test
    public void constructor_savedPositionOffScreen_opensWindowOnScreen() throws Exception {
        // far beyond any screen, as when the window was last on a monitor that is now disconnected
        Rectangle2D window = openWindowAt(new GuiSettings(740, 600, 100_000, 100_000));

        List<Rectangle2D> screens = onFxThread(() ->
                Screen.getScreens().stream().map(Screen::getVisualBounds).toList());
        assertTrue(WindowPlacement.isReachable(window, screens), "The window should open where it can be reached");
    }

    /** Creates the main window with {@code guiSettings} and returns where it is placed. */
    private Rectangle2D openWindowAt(GuiSettings guiSettings) throws Exception {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(guiSettings);
        Path data = temporaryFolder.resolve("addressbook.json");
        LogicManager logic = new LogicManager(new ModelManager(new AddressBook(), userPrefs),
                new StorageManager(new JsonAddressBookStorage(data),
                        new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        return onFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, logic, data);
            // A centred window only gets its position once it is shown
            stage.show();
            Rectangle2D bounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            stage.close();
            return bounds;
        });
    }
}
