package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Student's matriculation number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidMatric(String)}
 */
public class Matric {

    public static final String MESSAGE_CONSTRAINTS =
            "Matriculation numbers should start with a letter, followed by seven digits and one final letter, "
                    + "e.g. A0123456X";
    public static final String VALIDATION_REGEX = "[A-Za-z]\\d{7}[A-Za-z]";

    public final String value;

    /**
     * Constructs a {@code Matric}.
     *
     * @param matric A valid matriculation number.
     */
    public Matric(String matric) {
        requireNonNull(matric);
        checkArgument(isValidMatric(matric), MESSAGE_CONSTRAINTS);
        value = matric.toUpperCase();
    }

    /**
     * Returns true if a given string is a valid matriculation number.
     */
    public static boolean isValidMatric(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Matric otherMatric)) {
            return false;
        }

        return value.equals(otherMatric.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
