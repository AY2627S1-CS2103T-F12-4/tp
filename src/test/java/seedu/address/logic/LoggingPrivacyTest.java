package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

public class LoggingPrivacyTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_studentWorkflow_keepsDiagnosticsWithoutStudentFields() throws Exception {
        try (LogCapture logs = new LogCapture()) {
            ModelManager model = new ModelManager();
            LogicManager logic = createLogic(model);
            logic.execute("init grp/T09");
            logic.execute("init grp/T10");
            logic.execute("session grp/T09 w/1 d/2026-09-01");
            logic.execute("list grp/T10");
            String add = "add n/Private Auditname e/private.audit@example.com m/A0999999Z grp/T09 t/private-tag";
            assertTrue(logic.execute(add).getFeedbackToUser().contains("Private Auditname"));
            logic.execute("comment 1 c/PrivateHealthNote");
            assertEquals("PrivateHealthNote", model.getFilteredPersonList().getFirst().getComment().value);
            logic.execute("comment 1 c-/ReplacementPrivateNote");
            // Startup with an existing roster must not dump all its students at FINE.
            new ModelManager(model.getAddressBook(), new UserPrefs());
            logic.execute("comment 1 c-/");
            logic.execute("mark 1 w/1 s/present");
            logic.execute("mark 1 w/1 s/absent");
            logic.execute("edit 1 grp/T10");
            logic.execute("edit 1 e/changed.private@example.com");
            logic.execute("find n/Private Auditname");
            logic.execute("undo");
            logic.execute("redo");
            logic.execute("delete 1");
            assertTrue(model.getAddressBook().getPersonList().isEmpty());

            assertTrue(logs.getOutput().contains("Saved roster and history"));
            logs.assertDoesNotContain("Private Auditname", "private.audit@example.com", "A0999999Z",
                    "private-tag", "PrivateHealthNote", "ReplacementPrivateNote", "changed.private@example.com");
        }
    }

    @Test
    public void execute_parseAndCommandFailures_doNotLogInputOrStudentIdentifiers() throws Exception {
        Person student = new PersonBuilder().withName("Confidential Student").withEmail("confidential@example.com")
                .withMatric("A0888888X").build();
        AddressBook book = new AddressBookBuilder().withPerson(student).build();
        try (LogCapture logs = new LogCapture()) {
            ModelManager model = new ModelManager(book, new UserPrefs());
            LogicManager logic = createLogic(model);
            assertThrows(ParseException.class, () -> logic.execute("PrivateUnknownCommand confidential@example.com"));
            assertThrows(ParseException.class, () -> logic.execute("add n/Confidential Student e/PrivateInvalidEmail"));
            assertThrows(CommandException.class, () -> logic.execute(
                    "add n/Confidential Student e/confidential@example.com m/A0888888X grp/T09"));
            assertEquals(book, model.getAddressBook());
            logs.assertDoesNotContain("PrivateUnknownCommand", "PrivateInvalidEmail", "Confidential Student",
                    "confidential@example.com", "A0888888X");
        }
    }

    private LogicManager createLogic(ModelManager model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("roster.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
