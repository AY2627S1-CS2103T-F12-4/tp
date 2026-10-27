package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.VersionedAddressBook;
import seedu.address.model.person.Group;

/** Confirms that damaged optional history cannot replace or prevent loading a valid current roster. */
public class JsonHistoryValidationTest {
    @Test
    public void toVersionedModelType_incompleteHistory_keepsCurrentRoster() throws Exception {
        List<JsonSerializableAddressBook> states = List.of(emptySnapshot());
        List<String> commands = Arrays.asList((String) null);
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                null, commands, 0));
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                states, null, 0));
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                states, commands, null));
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                null, null, 0));
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                states, null, null));
    }

    @Test
    public void toVersionedModelType_nullOrInvalidSnapshot_keepsCurrentRoster() throws Exception {
        List<String> commands = Arrays.asList((String) null);
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                Arrays.asList((JsonSerializableAddressBook) null), commands, 0));
        JsonSerializableAddressBook invalidSnapshot =
                new JsonSerializableAddressBook(List.of(), List.of("invalid group"), List.of());
        assertHistoryDiscarded(new JsonSerializableAddressBook(List.of(), List.of("T09"), List.of(),
                List.of(invalidSnapshot), commands, 0));
    }

    @Test
    public void toVersionedModelType_invalidCurrentRoster_stillRejectsData() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of("invalid group"),
                List.of(), List.of(emptySnapshot()), Arrays.asList((String) null), 0);
        assertThrows(IllegalValueException.class, data::toVersionedModelType);
    }

    private static JsonSerializableAddressBook emptySnapshot() {
        return new JsonSerializableAddressBook(List.of(), List.of(), List.of());
    }

    private static void assertHistoryDiscarded(JsonSerializableAddressBook saved) throws Exception {
        VersionedAddressBook restored = saved.toVersionedModelType();
        assertEquals(List.of(new Group("T09")), restored.getGroupList());
        assertFalse(restored.canUndo());
        assertFalse(restored.canRedo());
        assertEquals(0, restored.getHistoryPointer());
    }
}
