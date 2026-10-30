package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final int MAX_LENGTH = 30;
    public static final int MAX_TAGS_PER_STUDENT = 10;
    public static final String MESSAGE_CONSTRAINTS = "Tags should be 1 to " + MAX_LENGTH + " characters long "
            + "and contain only letters and digits, optionally joined by single hyphens, "
            + "e.g. exchange or needs-follow-up.";
    public static final String MESSAGE_TOO_MANY_TAGS =
            "A student can have at most " + MAX_TAGS_PER_STUDENT + " tags.";
    // A hyphen may join two words, but cannot start or end a tag or appear twice in a row
    public static final String VALIDATION_REGEX = "\\p{Alnum}+(-\\p{Alnum}+)*";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        // The length is checked first, as the regex overflows the stack on very long input
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
