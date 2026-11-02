package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UnicodeWhitespaceWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_nonBreakingSpaceCommands_saveCorrectState() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        LogicManager logic = new LogicManager(model,
                new StorageManager(data, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("init grp/T81");
        logic.execute("add n/Beta Student e/beta@example.com m/A0888801A grp/T81");
        logic.execute("session grp/T81 w/1 d/2026-10-01");

        logic.execute("delete\u00a01\u00a0");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("undo\u2007");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        logic.execute("redo\u202f");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("undo\u00a0");
        logic.execute("clear\u00a0");
        assertTrue(model.getGroupList().isEmpty());
        assertTrue(model.getAddressBook().getSessionList().isEmpty());
        assertThrows(ParseException.class, () -> logic.execute("undo\u00a0unexpected"));
        logic.execute("undo\u00a0");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(1, model.getGroupList().size());
        assertEquals(1, model.getAddressBook().getSessionList().size());
        assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }
}
