package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AddressBook} that keeps snapshots of successfully committed states.
 */
public class VersionedAddressBook extends AddressBook {
    public static final int MAX_HISTORY_COMMANDS = 15;
    private static final int MAX_HISTORY_STATES = MAX_HISTORY_COMMANDS + 1;

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
        if (initialState instanceof VersionedAddressBook versioned) {
            restoreHistory(versioned.getHistoryStates(), versioned.getHistoryCommands(), versioned.getHistoryPointer());
            resetData(initialState);
        } else {
            resetHistory();
        }
    }

    /** Returns independent copies of all committed snapshots. */
    public List<AddressBook> getHistoryStates() {
        return addressBookStateList.stream().map(AddressBook::new).toList();
    }

    /** Returns the command names corresponding to the snapshots. */
    public List<String> getHistoryCommands() {
        return new ArrayList<>(commandDescriptionList);
    }

    /** Returns the index of the currently selected snapshot. */
    public int getHistoryPointer() {
        return currentStatePointer;
    }

    /**
     * Restores validated history without replacing the observable roster lists.
     * Keeps the latest bounded window containing the current state, even for older unlimited histories.
     */
    public void restoreHistory(List<AddressBook> states, List<String> commands, int pointer) {
        requireNonNull(states);
        requireNonNull(commands);
        if (states.isEmpty() || states.size() != commands.size() || pointer < 0 || pointer >= states.size()
                || states.stream().anyMatch(java.util.Objects::isNull) || commands.getFirst() != null
                || commands.subList(1, commands.size()).stream().anyMatch(name -> name == null || name.isBlank())) {
            throw new IllegalArgumentException("Invalid undo/redo history");
        }
        int firstState = Math.min(pointer, Math.max(0, states.size() - MAX_HISTORY_STATES));
        int endState = Math.min(states.size(), firstState + MAX_HISTORY_STATES);
        List<AddressBook> copies = states.subList(firstState, endState).stream().map(AddressBook::new).toList();
        addressBookStateList.clear();
        addressBookStateList.addAll(copies);
        commandDescriptionList.clear();
        commandDescriptionList.addAll(commands.subList(firstState, endState));
        commandDescriptionList.set(0, null);
        currentStatePointer = pointer - firstState;
        resetData(addressBookStateList.get(currentStatePointer));
        assertValidHistory();
    }

    /** Returns the name of the command that redo would reapply. */
    public String getRedoDescription() {
        if (!canRedo()) {
            throw new IllegalStateException("No later Roster state exists");
        }
        return commandDescriptionList.get(currentStatePointer + 1);
    }

    /** Saves the current state and the command that produced it. */
    public void commit(String commandDescription) {
        requireNonNull(commandDescription);
        while (addressBookStateList.size() > currentStatePointer + 1) {
            addressBookStateList.removeLast();
            commandDescriptionList.removeLast();
        }
        addressBookStateList.add(new AddressBook(this));
        commandDescriptionList.add(commandDescription);
        currentStatePointer++;
        if (addressBookStateList.size() > MAX_HISTORY_STATES) {
            addressBookStateList.removeFirst();
            commandDescriptionList.removeFirst();
            commandDescriptionList.set(0, null);
            currentStatePointer--;
        }
        assertValidHistory();
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
        assertValidHistory();
        return undoneCommand;
    }

    /** Returns whether a state restored by {@link #undo()} can be reapplied. */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }

    /** Reapplies the next committed state. */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("No later Roster state exists");
        }
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
        assertValidHistory();
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
        assertValidHistory();
    }

    /**
     * Asserts that every saved state has a matching command name and that the current state is one of them.
     */
    private void assertValidHistory() {
        assert addressBookStateList.size() <= MAX_HISTORY_STATES : "History must stay within its command limit";
        assert commandDescriptionList.getFirst() == null : "The history boundary has no preceding command";
        assert addressBookStateList.size() == commandDescriptionList.size()
                : "Every saved state should have a matching command name";
        assert currentStatePointer >= 0 && currentStatePointer < addressBookStateList.size()
                : "The current state should be one of the saved states";
    }
}
