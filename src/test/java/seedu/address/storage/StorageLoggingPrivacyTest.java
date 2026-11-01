package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

public class StorageLoggingPrivacyTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void read_legacyRoster_registersItsGroupWithoutLoggingTheStudent() throws Exception {
        Person student = new PersonBuilder().withName("Private StoredStudent").withEmail("stored.private@example.com")
                .withMatric("A0666666X").withComment("PrivateStoredNote").build();
        JsonSerializableAddressBook legacy = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(student)), List.of(), List.of());
        Path file = temporaryFolder.resolve("legacy.json");
        JsonUtil.saveJsonFile(legacy, file);

        try (LogCapture logs = new LogCapture()) {
            ReadOnlyAddressBook restored = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
            assertEquals(List.of(student), restored.getPersonList());
            assertEquals(List.of(student.getGroup()), restored.getGroupList());
            assertTrue(logs.getOutput().contains("Registered tutorial group"));
            logs.assertDoesNotContain("Private StoredStudent", "stored.private@example.com", "A0666666X",
                    "PrivateStoredNote");
        }
    }

    @Test
    public void read_malformedJson_reportsFailureWithoutLoggingItsContents() throws Exception {
        Path file = temporaryFolder.resolve("malformed.json");
        Files.writeString(file, "{\"persons\":\"private.json@example.com\"}");
        try (LogCapture logs = new LogCapture()) {
            DataLoadingException failure = assertThrows(
                    DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook());
            assertTrue(failure.getCause() instanceof java.io.IOException);
            assertTrue(logs.getOutput().contains("Error reading"));
            logs.assertDoesNotContain("private.json@example.com");
        }
    }

    @Test
    public void read_invalidStudent_reportsFailureWithoutDumpingTheRecord() throws Exception {
        JsonAdaptedPerson invalid = new JsonAdaptedPerson("Private InvalidStudent", "private.invalid@example.com",
                "A0555555X", "t09", "PrivateInvalidNote", List.of(), List.of());
        Path file = temporaryFolder.resolve("invalid.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(List.of(invalid), List.of(), List.of()), file);
        try (LogCapture logs = new LogCapture()) {
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook());
            assertTrue(logs.getOutput().contains("Illegal values found"));
            logs.assertDoesNotContain("Private InvalidStudent", "private.invalid@example.com", "A0555555X",
                    "PrivateInvalidNote");
        }
    }
}
