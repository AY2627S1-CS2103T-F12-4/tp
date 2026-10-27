package seedu.address.model.session.exceptions;

/** Signals that the requested session is not present in the session list. */
public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException() {
        super("The requested tutorial session does not exist");
    }
}
