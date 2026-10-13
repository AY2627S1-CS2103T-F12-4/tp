package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS = "Names must be 1-70 characters, contain only letters, "
            + "spaces, apostrophes or hyphens, and begin and end with a letter.";

    public static final String VALIDATION_REGEX = "\\p{L}(?:[\\p{L} '\u2019-]*\\p{L})?";

    public final String fullName;

    /**
     * Constructs a {@code Name}, trimming and collapsing whitespace.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        String normalizedName = normalizeWhitespace(name);
        checkArgument(isValidName(normalizedName), MESSAGE_CONSTRAINTS);
        fullName = normalizedName;
    }

    /**
     * Returns true if a given string is a valid name after whitespace normalization.
     */
    public static boolean isValidName(String test) {
        String normalizedName = normalizeWhitespace(test);
        return normalizedName.codePointCount(0, normalizedName.length()) <= 70
                && normalizedName.matches(VALIDATION_REGEX);
    }

    private static String normalizeWhitespace(String name) {
        return name.replaceAll("(?U)\\s+", " ").strip();
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
