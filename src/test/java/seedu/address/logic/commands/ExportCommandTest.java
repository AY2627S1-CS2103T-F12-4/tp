package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvAttendanceExporter;
import seedu.address.storage.CsvFileAccess;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ExportCommandTest {
    @TempDir
    public Path temporaryFolder;

    private final Person first = new PersonBuilder().withName("John Tan").withMatric("A0000001X")
            .withEmail("john@example.com").build();
    private final Person second = new PersonBuilder().withName("Alice Tan").withMatric("A0000002X")
            .withEmail("alice@example.com").withGroup("T10").build();
    private final AddressBook roster = new AddressBookBuilder().withPerson(first).withPerson(second).build();
    private final ModelManager model = new ModelManager(roster, new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_ignoresDisplayedSubsetAndPreservesSelectionAndData() throws Exception {
        model.showGroup(new Group("T09"));
        Path output = temporaryFolder.resolve("all.csv");
        CommandResult result = new ExportCommand(output, null, files()).execute(model);
        assertEquals(String.format(ExportCommand.MESSAGE_SUCCESS, 2, 2, output.toRealPath()),
                result.getFeedbackToUser());
        assertEquals(3, Files.readAllLines(output).size());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(new Group("T09"), model.getActiveGroup().orElseThrow());
        assertEquals(roster, model.getAddressBook());

        parser.parseCommand("find n/Unknown").execute(model);
        Path groupOutput = temporaryFolder.resolve("group.csv");
        new ExportCommand(groupOutput, new Group("T10"), files()).execute(model);
        assertEquals(List.of(CsvAttendanceExporter.HEADER,
                "A0000002X,Alice Tan,alice@example.com,T10,,,"), Files.readAllLines(groupOutput));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.getActiveGroup().isEmpty());
    }

    @Test
    public void execute_unknownGroupOrWriteFailure_leavesViewAndRecordsUntouched() {
        model.showGroup(new Group("T09"));
        Path output = temporaryFolder.resolve("unknown.csv");
        assertEquals(String.format(ListCommand.MESSAGE_GROUP_NOT_FOUND, "T99"),
                assertThrows(CommandException.class, () ->
                    new ExportCommand(output, new Group("T99"), files()).execute(model)).getMessage());
        assertFalse(Files.exists(output));
        assertThrows(CommandException.class, () ->
                new ExportCommand(temporaryFolder.resolve("missing/output.csv"), null, files()).execute(model));
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(new Group("T09"), model.getActiveGroup().orElseThrow());
        assertEquals(roster, model.getAddressBook());
    }

    @Test
    public void execute_existingFile_hasClearFeedbackAndPreservesContents() throws Exception {
        Path output = temporaryFolder.resolve("existing.csv");
        Files.writeString(output, "original");
        assertEquals(String.format(ExportCommand.MESSAGE_FILE_EXISTS, output),
                assertThrows(CommandException.class, () ->
                    new ExportCommand(output, null, files()).execute(model)).getMessage());
        assertEquals("original", Files.readString(output));
    }

    private CsvFileAccess files() {
        return new CsvFileAccess(temporaryFolder);
    }

    @Test
    public void execute_throughLogic_doesNotSaveRosterForExport() throws Exception {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Read-only roster must not be saved");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        parser.parseCommand("find n/John").execute(model);
        Path output = Files.createTempFile(Path.of("build"), "attendance-test-", ".csv").toAbsolutePath();
        Files.delete(output);
        try {
            logic.execute("export f/\"" + output + "\"");
            assertEquals(3, Files.readAllLines(output).size());
        } finally {
            Files.deleteIfExists(output);
        }
        assertFalse(Files.exists(failingStorage.getAddressBookFilePath()));
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(roster, model.getAddressBook());
    }
}
