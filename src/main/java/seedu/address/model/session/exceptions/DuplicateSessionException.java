package seedu.address.model.session.exceptions;

/**
 * Signals that an operation would result in duplicate tutorial sessions.
 */
public class DuplicateSessionException extends RuntimeException {
    public DuplicateSessionException() {
        super("Operation would result in duplicate tutorial sessions");
    }
}
