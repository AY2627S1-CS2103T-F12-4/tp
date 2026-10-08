package seedu.address.model.group.exceptions;

/**
 * Signals that an operation would result in duplicate tutorial groups.
 */
public class DuplicateGroupException extends RuntimeException {
    public DuplicateGroupException() {
        super("Operation would result in duplicate tutorial groups");
    }
}
