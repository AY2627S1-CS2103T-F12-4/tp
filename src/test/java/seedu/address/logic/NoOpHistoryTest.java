package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.VersionedAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class NoOpHistoryTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        logic = createLogic(model, storage);
        logic.execute("init grp/T09");
        logic.execute("add n/Alice Tan e/alice@example.com m/A0000001X grp/T09");
        logic.execute("session grp/T09 w/1 d/2026-10-20");
        logic.execute("mark 1 w/1 s/present");
        logic.execute("comment 1 c-/Bring notes");
    }

    @Test
    public void execute_noOpCommands_preserveHistoryAndSavedBytes() throws Exception {
        for (String command : List.of("edit 1 n/Alice Tan", "edit 1 e/ALICE@example.com", "edit 1 grp/t09",
                "edit 1 t/", "mark 1 w/1 s/present", "mark all w/1 s/present", "comment 1 c-/Bring notes")) {
            assertHistoryUnchanged(command);
        }

        assertEquals("Undid the most recent comment command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getComment().isEmpty());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_noOpAfterUndo_preservesRedoAcrossRestart() throws Exception {
        logic.execute("edit 1 n/Alicia Tan");
        logic.execute("undo");
        assertHistoryUnchanged("edit 1 n/Alice Tan");
        assertHistoryUnchanged("mark 1 w/1 s/present");
        assertTrue(model.canRedoAddressBook());

        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = createLogic(restarted, storage);
        assertEquals("Redid the most recent edit command.", restartedLogic.execute("redo").getFeedbackToUser());
        assertEquals("Alicia Tan", restarted.getAddressBook().getPersonList().getFirst().getName().fullName);
        restartedLogic.execute("undo");
        restartedLogic.execute("edit 1 n/Changed Name");
        assertFalse(restarted.canRedoAddressBook());
    }

    @Test
    public void execute_clearEmptyRoster_createsNeitherUndoStepNorFile() throws Exception {
        ModelManager emptyModel = new ModelManager();
        JsonAddressBookStorage emptyStorage = new JsonAddressBookStorage(temporaryFolder.resolve("empty.json"));
        createLogic(emptyModel, emptyStorage).execute("clear");
        assertFalse(emptyModel.canUndoAddressBook());
        assertFalse(Files.exists(emptyStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_groupAndSessionChangesWithoutStudents_stillCreateUndoSteps() throws Exception {
        logic.execute("init grp/T11");
        logic.execute("session grp/T11 w/1 d/2026-10-21");
        assertEquals("Undid the most recent session command.", logic.execute("undo").getFeedbackToUser());
        assertEquals("Undid the most recent init command.", logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_legacyDuplicateSnapshots_undoAndRedoPersistTheirHistoryPosition() throws Exception {
        VersionedAddressBook legacy = new VersionedAddressBook(model.getAddressBook());
        legacy.commit("edit");
        int lastPointer = legacy.getHistoryPointer();
        ModelManager legacyModel = new ModelManager(legacy, new UserPrefs());
        LogicManager legacyLogic = createLogic(legacyModel, storage);

        legacyLogic.execute("undo");
        VersionedAddressBook saved = (VersionedAddressBook) storage.readAddressBook().orElseThrow();
        assertEquals(lastPointer - 1, saved.getHistoryPointer());
        legacyLogic.execute("redo");
        saved = (VersionedAddressBook) storage.readAddressBook().orElseThrow();
        assertEquals(lastPointer, saved.getHistoryPointer());
    }

    private void assertHistoryUnchanged(String command) throws Exception {
        VersionedAddressBook before = new VersionedAddressBook(model.getAddressBook());
        String savedBefore = Files.readString(storage.getAddressBookFilePath());
        logic.execute(command);
        VersionedAddressBook after = (VersionedAddressBook) model.getAddressBook();
        assertEquals(before, after, command);
        assertEquals(before.getHistoryStates(), after.getHistoryStates(), command);
        assertEquals(before.getHistoryCommands(), after.getHistoryCommands(), command);
        assertEquals(before.getHistoryPointer(), after.getHistoryPointer(), command);
        assertEquals(savedBefore, Files.readString(storage.getAddressBookFilePath()), command);
    }

    private LogicManager createLogic(ModelManager target, JsonAddressBookStorage targetStorage) {
        return new LogicManager(target, new StorageManager(targetStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
