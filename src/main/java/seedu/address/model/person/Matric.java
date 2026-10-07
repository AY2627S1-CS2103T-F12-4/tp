package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's matriculation number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidMatric(String)}
 */
public class Matric {

    public static final String MESSAGE_CONSTRAINTS =
            "Matriculation numbers should be the letter A, followed by 7 digits and 1 letter, e.g. A0287654J.";

    /*
     * Letters are expected in uppercase: user input is converted to uppercase before it is validated.
     */
    public static final String VALIDATION_REGEX = "A\\d{7}[A-Z]";

    public final String value;

    /**
     * Constructs a {@code Matric}.
     *
     * @param matric A valid matriculation number.
     */
    public Matric(String matric) {
        requireNonNull(matric);
        checkArgument(isValidMatric(matric), MESSAGE_CONSTRAINTS);
        value = matric;
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
