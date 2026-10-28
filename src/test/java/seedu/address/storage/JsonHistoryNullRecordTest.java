package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.VersionedAddressBook;
import seedu.address.model.person.Group;

/** Exercises null records through the same JSON storage entry point used at startup. */
public class JsonHistoryNullRecordTest {
    private static final String EMPTY_ROSTER = "\"persons\":[],\"groups\":[\"T09\"],\"sessions\":[]";
    private static final String NULL_PERSON = "\"persons\":[null],\"groups\":[\"T09\"],\"sessions\":[]";
    private static final String NULL_SESSION = "\"persons\":[],\"groups\":[\"T09\"],\"sessions\":[null]";

    @TempDir
    public Path directory;

    @Test
    public void readAddressBook_nullPersonInHistory_discardsOnlyHistory() throws Exception {
        assertHistoryDiscarded(NULL_PERSON);
    }

    @Test
    public void readAddressBook_nullSessionInHistory_discardsOnlyHistory() throws Exception {
        assertHistoryDiscarded(NULL_SESSION);
    }

    @Test
    public void readAddressBook_nullTagInHistory_discardsOnlyHistory() throws Exception {
        assertHistoryDiscarded(personRoster("[null]", "[]"));
    }

    @Test
    public void readAddressBook_nullAttendanceInHistory_discardsOnlyHistory() throws Exception {
        assertHistoryDiscarded(personRoster("[]", "[null]"));
    }

    @Test
    public void readAddressBook_nullRecordsInCurrentRoster_rejectsData() throws Exception {
        for (String current : List.of(NULL_PERSON, NULL_SESSION, personRoster("[null]", "[]"),
                personRoster("[]", "[null]"))) {
            Path file = directory.resolve("invalid-current.json");
            Files.writeString(file, "{" + current + ",\"historyStates\":[{" + EMPTY_ROSTER
                    + "}],\"historyCommands\":[null],\"historyPointer\":0}");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    private void assertHistoryDiscarded(String damagedSnapshot) throws Exception {
        Path file = directory.resolve("roster.json");
        String json = "{" + EMPTY_ROSTER + ",\"historyStates\":[{" + damagedSnapshot
                + "}],\"historyCommands\":[null],\"historyPointer\":0}";
        Files.writeString(file, json);
        VersionedAddressBook restored = (VersionedAddressBook)
                new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(List.of(new Group("T09")), restored.getGroupList());
        assertEquals(0, restored.getPersonList().size());
        assertEquals(0, restored.getSessionList().size());
        assertFalse(restored.canUndo());
        assertFalse(restored.canRedo());
        assertEquals(1, restored.getHistoryStates().size());
        assertEquals(json, Files.readString(file));
    }

    private static String personRoster(String tags, String attendances) {
        return "\"persons\":[{\"name\":\"Alex Tan\",\"email\":\"alex@example.com\","
                + "\"matric\":\"A0123456X\",\"group\":\"T09\",\"tags\":" + tags
                + ",\"attendances\":" + attendances + "}],\"groups\":[\"T09\"],\"sessions\":[]";
    }
}
