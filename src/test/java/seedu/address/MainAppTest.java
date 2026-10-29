package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.ui.Ui;

public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void filePaths_default_areInLaunchFolder() {
        MainApp app = new MainApp();
        assertEquals(Paths.get("preferences.json"), app.getUserPrefsFilePath());
        assertEquals(Paths.get("data", "addressbook.json"), app.getAddressBookFilePath());
    }

    @Test
    public void init_noDataFile_startsWithSampleRoster() throws Exception {
        MainApp app = new TemporaryFolderApp(temporaryFolder.resolve("preferences.json"));
        app.init();

        assertEquals(SampleDataUtil.getSamplePersons().length, app.model.getAddressBook().getPersonList().size());
        assertEquals(temporaryFolder.resolve("data.json"), app.storage.getAddressBookFilePath());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        // Missing preferences are written out straight away
        assertTrue(Files.exists(temporaryFolder.resolve("preferences.json")));
    }

    @Test
    public void init_invalidDataFile_startsWithEmptyRoster() throws Exception {
        Files.writeString(temporaryFolder.resolve("data.json"), "not a roster");
        MainApp app = new TemporaryFolderApp(temporaryFolder.resolve("preferences.json"));
        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertTrue(app.model.getAddressBook().getGroupList().isEmpty());
    }

    @Test
    public void startAndStop_savesPreferences() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        MainApp app = new TemporaryFolderApp(prefsFile);
        app.init();
        Files.delete(prefsFile);
        StartRecordingUi ui = new StartRecordingUi();
        app.ui = ui;

        app.start(null);
        assertTrue(ui.isStarted);

        app.stop();
        assertTrue(Files.exists(prefsFile));
    }

    @Test
    public void stop_preferencesCannotBeSaved_doesNotThrow() throws Exception {
        // A folder in place of the preferences file makes every save fail
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Files.createDirectory(prefsFile);
        MainApp app = new TemporaryFolderApp(prefsFile);
        app.init();

        assertDoesNotThrow(app::stop);
    }

    /** Keeps Roster's files inside a test's temporary folder. */
    private class TemporaryFolderApp extends MainApp {
        private final Path userPrefsFilePath;

        TemporaryFolderApp(Path userPrefsFilePath) {
            this.userPrefsFilePath = userPrefsFilePath;
        }

        @Override
        protected Path getUserPrefsFilePath() {
            return userPrefsFilePath;
        }

        @Override
        protected Path getAddressBookFilePath() {
            return temporaryFolder.resolve("data.json");
        }
    }

    /** Records that the UI was started, without opening a window. */
    private static class StartRecordingUi implements Ui {
        private boolean isStarted;

        @Override
        public void start(Stage primaryStage) {
            isStarted = true;
        }
    }
}
