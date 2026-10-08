package seedu.address.model.group;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a tutorial group code.
 * Guarantees: immutable; is valid as declared in {@link #isValidGroup(String)}.
 */
public class Group {

    public static final String MESSAGE_CONSTRAINTS = "Tutorial group codes should contain one or two letters "
            + "followed by two digits.";
    public static final String VALIDATION_REGEX = "[A-Za-z]{1,2}\\d{2}";

    public final String code;

    /**
     * Constructs a normalized {@code Group}.
     */
    public Group(String code) {
        requireNonNull(code);
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        checkArgument(isValidGroup(normalizedCode), MESSAGE_CONSTRAINTS);
        this.code = normalizedCode;
    }

    /**
     * Returns true if {@code test} is a valid tutorial group code.
     */
    public static boolean isValidGroup(String test) {
        return test != null && test.trim().matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Group otherGroup && code.equals(otherGroup.code));
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
