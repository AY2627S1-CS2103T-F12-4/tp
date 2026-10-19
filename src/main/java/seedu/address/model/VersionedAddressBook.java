package seedu.address.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AddressBook} that keeps snapshots of successfully committed states.
 */
public class VersionedAddressBook extends AddressBook {
    private final List<AddressBook> addressBookStateList = new ArrayList<>();
    private final List<String> commandDescriptionList = new ArrayList<>();
    private int currentStatePointer;

    /** Creates an empty versioned address book. */
    public VersionedAddressBook() {
        this(new AddressBook());
    }

    /** Creates a versioned address book from {@code initialState}. */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        resetHistory();
    }

    /** Saves the current state and the command that produced it. */
    public void commit(String commandDescription) {
        while (addressBookStateList.size() > currentStatePointer + 1) {
            addressBookStateList.removeLast();
            commandDescriptionList.removeLast();
        }
        addressBookStateList.add(new AddressBook(this));
        commandDescriptionList.add(commandDescription);
        currentStatePointer++;
    }

    /** Returns whether an earlier committed state can be restored. */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /**
     * Restores the previous committed state and returns the command that was undone.
     */
    public String undo() {
        if (!canUndo()) {
            throw new IllegalStateException("No earlier Roster state exists");
        }
        String undoneCommand = commandDescriptionList.get(currentStatePointer);
        currentStatePointer--;
        resetData(addressBookStateList.get(currentStatePointer));
        return undoneCommand;
    }

    /** Returns whether a state restored by {@link #undo()} can be reapplied. */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }

    /** Reapplies the next state. Used to roll back an undo if saving fails. */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("No later Roster state exists");
        }
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Discards every earlier and later state, making the current state the new history boundary.
     */
    public void resetHistory() {
        addressBookStateList.clear();
        commandDescriptionList.clear();
        addressBookStateList.add(new AddressBook(this));
        commandDescriptionList.add(null);
        currentStatePointer = 0;
    }
}
