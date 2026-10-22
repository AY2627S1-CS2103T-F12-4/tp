package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
}
