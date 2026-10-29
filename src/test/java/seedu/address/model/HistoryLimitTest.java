package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Group;

public class HistoryLimitTest {
    @Test
    public void commit_fifteenChanges_canUndoAndRedoEveryChange() {
        VersionedAddressBook book = committedGroups(15);
        assertEquals(16, book.getHistoryStates().size());
        for (int group = 15; group >= 1; group--) {
            assertEquals("init " + group, book.undo());
            assertEquals(group - 1, book.getGroupList().size());
        }
        assertFalse(book.canUndo());
        for (int group = 1; group <= 15; group++) {
            assertEquals("init " + group, book.getRedoDescription());
            book.redo();
            assertEquals(group, book.getGroupList().size());
        }
        assertFalse(book.canRedo());
    }

    @Test
    public void commit_sixteenthChange_discardsOnlyOldestUndoStep() {
        VersionedAddressBook book = committedGroups(16);
        assertEquals(16, book.getHistoryStates().size());
        assertEquals(16, book.getHistoryCommands().size());
        assertNull(book.getHistoryCommands().getFirst());
        assertEquals(15, book.getHistoryPointer());
        for (int group = 16; group >= 2; group--) {
            assertEquals("init " + group, book.undo());
        }
        assertEquals(List.of(new Group("T01")), book.getGroupList());
        assertThrows(IllegalStateException.class, book::undo);
        assertEquals("init 2", book.getRedoDescription());
        book.redo();
        assertEquals(2, book.getGroupList().size());
    }

    @Test
    public void commit_manyChangesAndRepeatedUndoRedo_keepsHistoryBounded() {
        VersionedAddressBook book = committedGroups(60);
        assertEquals(60, book.getGroupList().size());
        List<AddressBook> states = book.getHistoryStates();
        List<String> commands = book.getHistoryCommands();
        for (int repeat = 0; repeat < 50; repeat++) {
            book.undo();
            book.redo();
            assertEquals(states, book.getHistoryStates());
            assertEquals(commands, book.getHistoryCommands());
        }
        for (int step = 0; step < 15; step++) {
            book.undo();
        }
        assertEquals(45, book.getGroupList().size());
        assertFalse(book.canUndo());
    }

    @Test
    public void commit_afterUndo_discardsRedoWithoutDiscardingMorePastStates() {
        VersionedAddressBook book = committedGroups(20);
        book.undo();
        book.undo();
        book.addGroup(new Group("T99"));
        book.commit("init 99");
        assertFalse(book.canRedo());
        assertEquals(15, book.getHistoryStates().size());
        assertEquals("init 99", book.undo());
        assertEquals(18, book.getGroupList().size());
        assertFalse(book.hasGroup(new Group("T99")));
        assertEquals("init 99", book.getRedoDescription());
        book.redo();
        assertTrue(book.hasGroup(new Group("T99")));
    }

    @Test
    public void restoreHistory_oversized_keepsLatestWindowContainingCurrentState() {
        List<AddressBook> states = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        AddressBook current = new AddressBook();
        states.add(new AddressBook(current));
        commands.add(null);
        for (int group = 1; group <= 30; group++) {
            current.addGroup(group(group));
            states.add(new AddressBook(current));
            commands.add("init " + group);
        }
        for (int pointer : new int[]{0, 4, 15, 25, 28, 30}) {
            VersionedAddressBook book = new VersionedAddressBook();
            book.restoreHistory(states, commands, pointer);
            int first = Math.min(pointer, 15);
            assertEquals(states.get(pointer), book);
            assertEquals(16, book.getHistoryStates().size());
            assertEquals(pointer - first, book.getHistoryPointer());
            assertEquals(states.subList(first, first + 16), book.getHistoryStates());
            assertNull(book.getHistoryCommands().getFirst());
            assertEquals(commands.subList(first + 1, first + 16), book.getHistoryCommands().subList(1, 16));
            assertEquals("init " + (first + 1), commands.get(first + 1));
        }
    }

    @Test
    public void constructor_copiesHistoryAtLimitWithoutChangingUndoRedoPosition() {
        VersionedAddressBook original = committedGroups(20);
        original.undo();
        original.undo();
        VersionedAddressBook copy = new VersionedAddressBook(original);
        assertEquals(original, copy);
        assertEquals(original.getHistoryStates(), copy.getHistoryStates());
        assertEquals(original.getHistoryCommands(), copy.getHistoryCommands());
        assertEquals(original.getHistoryPointer(), copy.getHistoryPointer());
        copy.redo();
        assertEquals(19, copy.getGroupList().size());
        assertEquals(18, original.getGroupList().size());
    }

    private static VersionedAddressBook committedGroups(int count) {
        VersionedAddressBook book = new VersionedAddressBook();
        for (int group = 1; group <= count; group++) {
            book.addGroup(group(group));
            book.commit("init " + group);
            assertTrue(book.getHistoryStates().size() <= 16);
        }
        return book;
    }

    private static Group group(int number) {
        return new Group(String.format("T%02d", number));
    }
}
