package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.StudentListViewState;
import seedu.address.model.VersionedAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ExitHelpArgumentsWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_rejectedArguments_preservesRosterViewStorageAndRedo() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        LogicManager logic = new LogicManager(model,
                new StorageManager(data, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("init grp/T81");
        logic.execute("init grp/T82");
        logic.execute("add n/Beta Student e/beta@example.com m/A0888801A grp/T81");
        logic.execute("comment 1 c-/Follow up");
        logic.execute("undo");
        logic.execute("list grp/T81");
        VersionedAddressBook before = new VersionedAddressBook(model.getAddressBook());
        StudentListViewState view = model.getStudentListViewState();
        byte[] saved = Files.readAllBytes(data.getAddressBookFilePath());

        for (String command : List.of("exit unexpected", "help grp/T81", "exit\u00a03", "help\u202fall")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            VersionedAddressBook after = (VersionedAddressBook) model.getAddressBook();
            assertEquals(before, after);
            assertEquals(before.getHistoryStates(), after.getHistoryStates());
            assertEquals(before.getHistoryCommands(), after.getHistoryCommands());
            assertEquals(before.getHistoryPointer(), after.getHistoryPointer());
            assertEquals(view, model.getStudentListViewState());
            assertTrue(Arrays.equals(saved, Files.readAllBytes(data.getAddressBookFilePath())));
        }
        logic.execute("redo");
        assertEquals("Follow up", model.getAddressBook().getPersonList().getFirst().getComment().value);
    }
}
