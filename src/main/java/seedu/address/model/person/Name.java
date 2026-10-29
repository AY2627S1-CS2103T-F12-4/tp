package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.StringUtil.normalizeWhitespace;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS = "Names must be 1-70 characters, "
            + "contain letters and their accents, "
            + "spaces, apostrophes, hyphens, full stops or s/o, d/o, a/l, a/p abbreviations, "
            + "begin with a letter, and end with a letter, accent or full stop.";

    public static final String VALIDATION_REGEX = "\\p{L}(?:[\\p{L}\\p{M} .'\u2019-]*[\\p{L}\\p{M}.])?";
    // Match complete name words so that s/o is accepted without treating s/other as a name abbreviation.
    public static final String RELATIONSHIP_CONNECTOR_REGEX = "(?iU)(?<!\\S)(?:s/o|d/o|a/l|a/p)(?!\\S)";

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
                && normalizedName.replaceAll(RELATIONSHIP_CONNECTOR_REGEX, "relative").matches(VALIDATION_REGEX);
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
