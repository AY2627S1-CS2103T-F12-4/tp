package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;

public class VersionedAddressBookTest {
    @Test
    public void constructor_empty_hasNoUndoOrRedoHistory() {
        VersionedAddressBook book = new VersionedAddressBook();
        assertEquals(new AddressBook(), book);
        assertFalse(book.canUndo());
        assertFalse(book.canRedo());
    }

    @Test
    public void undo_withoutHistory_throwsAndPreservesState() {
        VersionedAddressBook book = new VersionedAddressBook();
        assertThrows(IllegalStateException.class, book::undo);
        assertEquals(new AddressBook(), book);
        assertFalse(book.canUndo());
        assertFalse(book.canRedo());
    }

    @Test
    public void redo_withoutFutureState_throwsAndPreservesState() {
        VersionedAddressBook book = new VersionedAddressBook();
        book.commit("init");
        assertThrows(IllegalStateException.class, book::redo);
        assertEquals(new AddressBook(), book);
        assertEquals("init", book.undo());
        book.redo();
        assertFalse(book.canRedo());
    }

    @Test
    public void getRedoDescription_withoutFutureState_throws() {
        VersionedAddressBook book = new VersionedAddressBook();
        assertThrows(IllegalStateException.class, book::getRedoDescription);
        book.commit("init");
        book.undo();
        assertEquals("init", book.getRedoDescription());
        book.redo();
        assertThrows(IllegalStateException.class, book::getRedoDescription);
    }

    @Test
    public void restoreHistory_invalidStructure_preservesExistingHistory() {
        VersionedAddressBook book = new VersionedAddressBook();
        book.addGroup(new Group("T09"));
        book.commit("init");
        List<AddressBook> states = book.getHistoryStates();
        List<String> commands = book.getHistoryCommands();

        assertThrows(NullPointerException.class, () -> book.restoreHistory(null, commands, 1));
        assertThrows(NullPointerException.class, () -> book.restoreHistory(states, null, 1));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(List.of(), List.of(), 0));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(states, List.of(), 0));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(states, commands, -1));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(states, commands, states.size()));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(
                Arrays.asList(new AddressBook(), null), commands, 0));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(
                states, List.of("invalid initial command", "init"), 0));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(
                states, Arrays.asList(null, null), 0));
        assertThrows(IllegalArgumentException.class, () -> book.restoreHistory(
                states, Arrays.asList(null, "  "), 0));

        assertEquals(states, book.getHistoryStates());
        assertEquals(commands, book.getHistoryCommands());
        assertEquals(1, book.getHistoryPointer());
        assertEquals("init", book.undo());
        assertEquals(new AddressBook(), book);
    }

    @Test
    public void restoreHistory_defensivelyCopiesCallerOwnedSnapshotsAndCommands() {
        AddressBook initial = new AddressBook();
        AddressBook changed = new AddressBook();
        changed.addGroup(new Group("T09"));
        List<AddressBook> states = new ArrayList<>(List.of(initial, changed));
        List<String> commands = new ArrayList<>(Arrays.asList(null, "init"));
        VersionedAddressBook book = new VersionedAddressBook();
        book.restoreHistory(states, commands, 0);

        changed.addGroup(new Group("T10"));
        states.clear();
        commands.set(1, "corrupted");
        book.getHistoryStates().get(1).addGroup(new Group("T11"));
        book.getHistoryCommands().set(1, "corrupted");

        assertEquals("init", book.getRedoDescription());
        book.redo();
        assertEquals(List.of(new Group("T09")), book.getGroupList());
        assertEquals("init", book.undo());
        assertEquals(new AddressBook(), book);
    }

}
