package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ClearCommandWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_clearWithGroup_rejectsInputAndPreservesRosterStorageAndHistory() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage rosterFile = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(rosterFile,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("init grp/T91");
        logic.execute("init grp/T92");
        logic.execute("add n/Alice Tan e/alice@example.com m/A0999001A grp/T91");
        AddressBook beforeLastAdd = new AddressBook(model.getAddressBook());
        logic.execute("add n/Bob Lim e/bob@example.com m/A0999002B grp/T92");
        AddressBook beforeClear = new AddressBook(model.getAddressBook());

        assertThrows(ParseException.class, () -> logic.execute("clear grp/T91"));
        assertEquals(beforeClear, model.getAddressBook());
        assertEquals(beforeClear, rosterFile.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals(beforeLastAdd, model.getAddressBook());
        logic.execute("redo");
        assertEquals(beforeClear, model.getAddressBook());

        logic.execute("clear");
        assertEquals(new AddressBook(), model.getAddressBook());
        logic.execute("undo");
        assertEquals(beforeClear, model.getAddressBook());
    }
}
