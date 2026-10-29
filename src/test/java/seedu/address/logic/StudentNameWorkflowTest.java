package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class StudentNameWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addEditFindAndReload_preservesInitialsAbbreviationsAndAccents() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage rosterFile = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(rosterFile,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("init grp/T09");
        List<String> names = List.of("J. Tan", "Ravi s/o Kumar", "Rani D/O Kumar",
                "Arun a/l Ravi", "Meena A/P Kumar", "E\u0301lodie Tan", "Tan Ah Kow (Mike)");
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            logic.execute("add n/" + name + " e/student" + i + "@example.com"
                    + " m/A000000" + i + "X grp/T09");
            logic.execute("find n/" + name);
            assertEquals(1, model.getFilteredPersonList().size(), name);
            assertEquals(name, model.getFilteredPersonList().getFirst().getName().fullName);
            // Editing uses the same name contract and keeps the typed spelling.
            logic.execute("edit 1 n/" + name);
            assertEquals(model.getAddressBook(), rosterFile.readAddressBook().orElseThrow());
        }
        logic.execute("find n/S/O");
        assertEquals("Ravi s/o Kumar", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals(names.size(), model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_prefixLikeTypos_stillRejectsUnknownArguments() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("roster.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("init grp/T09");
        for (String input : List.of("s/other", "s/oops", "x/foo")) {
            assertEquals(AddCommand.MESSAGE_UNKNOWN_PARAMETER,
                    assertThrows(ParseException.class, () -> logic.execute("add n/Ravi " + input
                            + " e/ravi@example.com m/A0000001X grp/T09")).getMessage());
            assertEquals(FindCommand.MESSAGE_UNKNOWN_PARAMETER,
                    assertThrows(ParseException.class, () -> logic.execute("find n/Ravi " + input)).getMessage());
            assertEquals(EditCommand.MESSAGE_UNKNOWN_PARAMETER,
                    assertThrows(ParseException.class, () -> logic.execute("edit 1 n/Ravi " + input)).getMessage());
            assertEquals(0, model.getAddressBook().getPersonList().size());
        }
    }
}
