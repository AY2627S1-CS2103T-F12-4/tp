package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.FieldCompletion;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.VersionedAddressBook;
import seedu.address.model.person.Group;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises undo/redo through the real parser, model and JSON storage, including simulated restarts. */
public class PersistentHistoryTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        storage = new JsonAddressBookStorage(directory.resolve("roster.json"));
        model = new ModelManager();
        logic = createLogic(storage);
    }

    @Test
    public void restart_keepsBothUndoAndRedoWithTheirCommandNames() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("add n/Alex Tan e/alex@example.com m/A0123456X grp/T09");
        logic.execute("edit 1 n/Alex Lim");
        logic.execute("undo");
        restart();
        assertEquals("Alex Tan", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertEquals("Redid the most recent edit command.", logic.execute("redo").getFeedbackToUser());
        restart();
        assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        logic.execute("undo");
        assertEquals("Undid the most recent add command.", logic.execute("undo").getFeedbackToUser());
        logic.execute("undo");
        restart();
        assertFalse(model.canUndoAddressBook());
        assertEquals("Redid the most recent init command.", logic.execute("redo").getFeedbackToUser());
        logic.execute("redo");
        logic.execute("redo");
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void newChange_discardsRedoAcrossRestart() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("undo");
        logic.execute("init grp/T10");
        restart();
        assertFalse(model.canRedoAddressBook());
        assertTrue(model.hasGroup(new Group("T10")));
        assertFalse(model.hasGroup(new Group("T09")));
        logic.execute("undo");
        assertTrue(model.getGroupList().isEmpty());
    }

    @Test
    public void readOnlyAndFailedCommands_preserveRedo() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("undo");
        logic.execute("list");
        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertThrows(ParseException.class, () -> logic.execute("redo extra"));
        logic.execute("redo");
        assertTrue(model.hasGroup(new Group("T09")));
    }

    @Test
    public void emptyHistory_redoFailsWithoutCreatingFile() {
        assertThrows(CommandException.class, () -> logic.execute("redo"));
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void failedSave_preservesRedoAndDiskForAllHistoryActions() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("init grp/T10");
        logic.execute("undo");
        String saved = Files.readString(storage.getAddressBookFilePath());
        JsonAddressBookStorage failing = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        Logic failingLogic = createLogic(failing);
        for (String command : List.of("redo", "undo", "init grp/T11", "clear")) {
            assertThrows(CommandException.class, () -> failingLogic.execute(command));
            assertTrue(model.hasGroup(new Group("T09")));
            assertFalse(model.hasGroup(new Group("T10")));
            assertFalse(model.hasGroup(new Group("T11")));
            assertTrue(model.canUndoAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        logic.execute("redo");
        assertTrue(model.hasGroup(new Group("T10")));
        restart();
        assertTrue(model.hasGroup(new Group("T10")));
    }

    @Test
    public void deleteAndClear_restoreStudentsAndSessionAttendanceAcrossRestart() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("session grp/T09 w/1 d/2026-08-11");
        logic.execute("add n/Alex Tan e/alex@example.com m/A0123456X grp/T09");
        logic.execute("mark 1 w/1 s/present");
        ReadOnlyAddressBook expected = new seedu.address.model.AddressBook(model.getAddressBook());
        logic.execute("delete all");
        restart();
        logic.execute("undo");
        assertEquals(expected, model.getAddressBook());
        logic.execute("clear");
        restart();
        logic.execute("undo");
        assertEquals(expected, model.getAddressBook());
        logic.execute("redo");
        restart();
        assertTrue(model.getGroupList().isEmpty());
    }

    @Test
    public void legacyFile_loadsWithoutHistoryAndCanStartNewHistory() throws Exception {
        Files.writeString(storage.getAddressBookFilePath(),
                "{\"persons\":[],\"groups\":[\"T09\"],\"sessions\":[]}");
        restart();
        assertTrue(model.hasGroup(new Group("T09")));
        assertFalse(model.canUndoAddressBook());
        logic.execute("init grp/T10");
        restart();
        logic.execute("undo");
        assertTrue(model.hasGroup(new Group("T09")));
        assertFalse(model.hasGroup(new Group("T10")));
    }

    @Test
    public void mismatchedOrInvalidHistory_keepsCurrentRoster() throws Exception {
        logic.execute("init grp/T09");
        String original = Files.readString(storage.getAddressBookFilePath());
        for (String changed : List.of(original.replaceFirst("T09", "T10"),
                original.replace("\"historyPointer\" : 1", "\"historyPointer\" : 999"))) {
            assertFalse(original.equals(changed));
            Files.writeString(storage.getAddressBookFilePath(), changed);
            restart();
            assertEquals(1, model.getGroupList().size());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
        }
    }

    @Test
    public void historyLimit_growingRoster_keepsFifteenChangesAcrossUndoRedoAndRestart() throws Exception {
        logic.execute("init grp/T09");
        for (int student = 1; student <= 40; student++) {
            logic.execute("add n/Student " + (char) ('A' + student % 26)
                    + " e/student" + student + "@example.com m/" + String.format("A%07dA", student) + " grp/T09");
            assertTrue(((VersionedAddressBook) model.getAddressBook()).getHistoryStates().size() <= 16);
        }
        assertEquals(40, model.getAddressBook().getPersonList().size());
        JsonNode saved = new ObjectMapper().readTree(Files.readString(storage.getAddressBookFilePath()));
        assertEquals(16, saved.path("historyStates").size());
        assertEquals(16, saved.path("historyCommands").size());
        assertTrue(saved.path("historyCommands").get(0).isNull());
        restart();
        for (int step = 0; step < 15; step++) {
            assertEquals("Undid the most recent add command.", logic.execute("undo").getFeedbackToUser());
        }
        assertEquals(25, model.getAddressBook().getPersonList().size());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        restart();
        for (int step = 0; step < 5; step++) {
            logic.execute("redo");
        }
        restart();
        assertEquals(30, model.getAddressBook().getPersonList().size());
        for (int step = 0; step < 10; step++) {
            assertEquals("Redid the most recent add command.", logic.execute("redo").getFeedbackToUser());
        }
        assertEquals(40, model.getAddressBook().getPersonList().size());
        assertThrows(CommandException.class, () -> logic.execute("redo"));
        saved = new ObjectMapper().readTree(Files.readString(storage.getAddressBookFilePath()));
        assertEquals(16, saved.path("historyStates").size());
    }

    @Test
    public void historyLimit_repeatedFixedSizeChanges_stopGrowingSavedHistory() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("add n/Alex Tan e/alex@example.com m/A0123456X grp/T09");
        long sizeAtLimit = 0;
        for (int change = 1; change <= 80; change++) {
            logic.execute("comment 1 c-/Revision " + String.format("%03d", change));
            if (change == 20) {
                sizeAtLimit = Files.size(storage.getAddressBookFilePath());
            }
        }
        assertTrue(sizeAtLimit > 0);
        assertEquals(sizeAtLimit, Files.size(storage.getAddressBookFilePath()));
        restart();
        assertEquals("Revision 080", model.getAddressBook().getPersonList().getFirst().getComment().value);
        for (int step = 0; step < 15; step++) {
            logic.execute("undo");
        }
        assertEquals("Revision 065", model.getAddressBook().getPersonList().getFirst().getComment().value);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void failedSave_atHistoryLimit_restoresOldestUndoStepAndSavedFile() throws Exception {
        for (int group = 0; group < 15; group++) {
            logic.execute(String.format("init grp/T%02d", group));
        }
        VersionedAddressBook before = new VersionedAddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        Logic failingLogic = createLogic(new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated save failure at the history limit");
            }
        });
        assertThrows(CommandException.class, () -> failingLogic.execute("init grp/T99"));
        VersionedAddressBook restored = (VersionedAddressBook) model.getAddressBook();
        assertEquals(before, restored);
        assertEquals(before.getHistoryStates(), restored.getHistoryStates());
        assertEquals(before.getHistoryCommands(), restored.getHistoryCommands());
        assertEquals(before.getHistoryPointer(), restored.getHistoryPointer());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        for (int step = 0; step < 15; step++) {
            logic.execute("undo");
        }
        assertTrue(model.getGroupList().isEmpty());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void autocomplete_suggestsRedo() {
        assertEquals("redo", FieldCompletion.suggest("re", 2, List.of()).getFirst().value());
    }

    private Logic createLogic(JsonAddressBookStorage addressStorage) {
        return new LogicManager(model, new StorageManager(addressStorage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    private void restart() throws Exception {
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic(storage);
    }
}
