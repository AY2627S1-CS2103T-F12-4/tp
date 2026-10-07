package seedu.address.model.attendance;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents whether a student was present or absent at a session.
 * A student who has not been marked for a session has no status at all, rather than a third value.
 */
public enum Status {
    PRESENT,
    ABSENT;

    public static final String MESSAGE_CONSTRAINTS = "Status should be either 'present' or 'absent'.";

    /**
     * Returns true if a given string names a status, ignoring letter case.
     */
    public static boolean isValidStatus(String test) {
        requireNonNull(test);
        for (Status status : values()) {
            if (status.name().equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the status named by {@code status}, ignoring letter case.
     *
     * @param status A valid status name.
     */
    public static Status fromString(String status) {
        requireNonNull(status);
        checkArgument(isValidStatus(status), MESSAGE_CONSTRAINTS);
        return valueOf(status.toUpperCase(Locale.ROOT));
    }
}
