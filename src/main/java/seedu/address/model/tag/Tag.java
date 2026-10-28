package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 * <p>
 * Tags that differ only in letter case are equal, so a student cannot have both {@code exchange} and
 * {@code Exchange}. A set of tags keeps the spelling that was added first.
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tags should contain only letters and digits, "
            + "optionally joined by single hyphens, e.g. exchange or needs-follow-up.";
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
        return test.matches(VALIDATION_REGEX);
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

        return tagName.equalsIgnoreCase(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        // Must agree with equals, which ignores letter case
        return tagName.toLowerCase(Locale.ROOT).hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
