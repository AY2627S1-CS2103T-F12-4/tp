package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Group;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path DUPLICATE_SESSION_FILE = TEST_DATA_FOLDER.resolve("duplicateSessionAddressBook.json");
    private static final Path UNKNOWN_SESSION_FILE = TEST_DATA_FOLDER.resolve("unknownSessionAddressBook.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndLoad_emptyGroup_isRetained() throws Exception {
        AddressBook book = new AddressBook();
        book.addGroup(new Group("T11"));
        Path file = temporaryFolder.resolve("empty-group.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(book), file);
        AddressBook restored = JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).get().toModelType();
        assertEquals(book, restored);
        assertEquals(List.of(new Group("T11")), restored.getGroupList());
    }

    @Test
    public void toModelType_invalidGroups_rejectsMalformedOrDuplicateCodes() {
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableAddressBook(List.of(), List.of(), List.of("T9")).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableAddressBook(List.of(), List.of(), List.of("T09", "t09")).toModelType());
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateSessions_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_SESSION_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_SESSION,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_attendanceForUnknownSession_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(UNKNOWN_SESSION_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_UNKNOWN_SESSION,
                dataFromFile::toModelType);
    }

}
