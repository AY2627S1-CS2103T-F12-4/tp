package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a tutorial group code.
 * Guarantees: immutable; is valid as declared in {@link #isValidGroup(String)}.
 */
public class Group {

    public static final String MESSAGE_CONSTRAINTS =
            "Group codes should be one or two letters followed by two digits, e.g. T09.";
    public static final String VALIDATION_REGEX = "[A-Z]{1,2}\\d{2}";

    public final String value;

    /**
     * Constructs a {@code Group}.
     *
     * @param group A valid, normalized group code.
     */
    public Group(String group) {
        requireNonNull(group);
        checkArgument(isValidGroup(group), MESSAGE_CONSTRAINTS);
        value = group;
    }

    /**
     * Returns true if {@code test} is a valid normalized tutorial group code.
     */
    public static boolean isValidGroup(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Group otherGroup && value.equals(otherGroup.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
