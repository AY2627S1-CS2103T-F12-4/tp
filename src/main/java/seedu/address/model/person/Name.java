package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS = "Names must be 1-70 characters, "
            + "contain letters and their accents, "
            + "spaces, apostrophes, hyphens, full stops or s/o, d/o, a/l, a/p abbreviations, "
            + "begin with a letter, and end with a letter, accent or full stop. "
            + "A later part of the name may be in brackets, e.g. Tan Ah Kow (Mike).";

    public static final String VALIDATION_REGEX = "\\p{L}(?:[\\p{L}\\p{M} .'\u2019-]*[\\p{L}\\p{M}.])?";
    // Match complete name words so that s/o is accepted without treating s/other as a name abbreviation.
    public static final String RELATIONSHIP_CONNECTOR_REGEX = "(?iU)(?<!\\S)(?:s/o|d/o|a/l|a/p)(?!\\S)";
    // A bracketed part, such as a preferred name, follows a space and is a whole word or words. Its brackets
    // are removed before the rest of the name is checked, so its contents follow the usual rules.
    private static final String BRACKETED_PART_REGEX = "(?<= )\\((\\p{L}(?:[^()]*[\\p{L}\\p{M}.])?)\\)(?= |$)";

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
                && normalizedName.replaceAll(RELATIONSHIP_CONNECTOR_REGEX, "relative")
                        .replaceAll(BRACKETED_PART_REGEX, "$1")
                        .matches(VALIDATION_REGEX);
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
