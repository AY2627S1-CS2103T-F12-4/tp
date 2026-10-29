package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.VersionedAddressBook;
import seedu.address.model.person.Group;

public class JsonHistoryLimitTest {
    @TempDir
    public Path directory;

    @Test
    public void readAddressBook_oversizedLegacyHistory_preservesCurrentRosterAndBoundsNextSave() throws Exception {
        List<JsonSerializableAddressBook> snapshots = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        snapshots.add(new JsonSerializableAddressBook(List.of(), groups, List.of()));
        commands.add(null);
        for (int group = 1; group <= 30; group++) {
            groups.add(String.format("T%02d", group));
            snapshots.add(new JsonSerializableAddressBook(List.of(), groups, List.of()));
            commands.add("init " + group);
        }
        Path file = directory.resolve("legacy-history.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (int pointer : new int[]{0, 4, 25, 28, 30}) {
            JsonSerializableAddressBook saved = new JsonSerializableAddressBook(List.of(), groups.subList(0, pointer),
                    List.of(), snapshots, commands, pointer);
            JsonUtil.saveJsonFile(saved, file);
            String originalFile = Files.readString(file);

            VersionedAddressBook restored = (VersionedAddressBook) storage.readAddressBook().orElseThrow();
            assertEquals(groups.subList(0, pointer).stream().map(Group::new).toList(), restored.getGroupList());
            assertEquals(16, restored.getHistoryStates().size());
            assertEquals(pointer - Math.min(pointer, 15), restored.getHistoryPointer());
            assertNull(restored.getHistoryCommands().getFirst());
            assertEquals(originalFile, Files.readString(file));

            storage.saveAddressBook(restored);
            JsonNode trimmed = new ObjectMapper().readTree(Files.readString(file));
            assertEquals(16, trimmed.path("historyStates").size());
            assertEquals(16, trimmed.path("historyCommands").size());
            assertEquals(pointer, trimmed.path("groups").size());
            VersionedAddressBook reloaded = (VersionedAddressBook) storage.readAddressBook().orElseThrow();
            assertEquals(restored, reloaded);
            assertEquals(restored.getHistoryStates(), reloaded.getHistoryStates());
            assertEquals(restored.getHistoryCommands(), reloaded.getHistoryCommands());
            assertEquals(restored.getHistoryPointer(), reloaded.getHistoryPointer());
        }
    }
}
