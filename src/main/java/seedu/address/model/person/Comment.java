package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a TA's free-text note about a Student.
 * Guarantees: immutable; is valid as declared in {@link #isValidComment(String)}
 */
public class Comment {

    /** The absent comment, so that a Student never holds a null one. */
    public static final Comment EMPTY = new Comment();

    public static final int MAX_LENGTH = 200;

    public static final String SEPARATOR = "; ";

    public static final String MESSAGE_CONSTRAINTS =
            "Comments should be printable text of 1 to " + MAX_LENGTH + " characters, e.g. attentive in tutorials";

    public static final String MESSAGE_TOO_LONG =
            "The resulting comment would be %1$d characters, which is over the limit of " + MAX_LENGTH + ".";

    public final String value;

    /** Constructs the empty comment. */
    private Comment() {
        value = "";
    }

    /**
     * Constructs a {@code Comment}.
     *
     * @param comment A valid comment.
     */
    public Comment(String comment) {
        requireNonNull(comment);
        String normalizedComment = normalize(comment);
        checkArgument(isValidComment(normalizedComment), MESSAGE_CONSTRAINTS);
        value = normalizedComment;
    }

    /**
     * Returns {@code comment} with surrounding whitespace removed and each run of internal
     * whitespace collapsed to a single space.
     */
    public static String normalize(String comment) {
        requireNonNull(comment);
        return comment.replaceAll("(?U)\\s+", " ").strip();
    }

    /**
     * Returns true if {@code test} is a valid comment once normalized.
     * Rejects an empty value: the absent comment is {@link #EMPTY}, not a blank one.
     */
    public static boolean isValidComment(String test) {
        if (test == null) {
            return false;
        }
        String normalizedTest = normalize(test);
        return !normalizedTest.isEmpty() && normalizedTest.length() <= MAX_LENGTH
                && normalizedTest.chars().noneMatch(Character::isISOControl);
    }

    /** Returns true if this is the absent comment. */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Returns this comment with {@code addition} appended after a separator.
     * Appending to the empty comment returns {@code addition} on its own, so the first
     * note of a student does not begin with a separator.
     *
     * @throws IllegalArgumentException if the combined comment would exceed {@link #MAX_LENGTH}.
     */
    public Comment append(Comment addition) {
        requireNonNull(addition);
        if (isEmpty()) {
            return addition;
        }
        if (addition.isEmpty()) {
            return this;
        }
        String combined = value + SEPARATOR + addition.value;
        checkArgument(combined.length() <= MAX_LENGTH, String.format(MESSAGE_TOO_LONG, combined.length()));
        return new Comment(combined);
    }

    /**
     * Returns the length this comment would have after {@code addition} is appended, so that a
     * caller can report the overflow before attempting it.
     */
    public int lengthAfterAppending(Comment addition) {
        requireNonNull(addition);
        if (isEmpty() || addition.isEmpty()) {
            return Math.max(value.length(), addition.value.length());
        }
        return value.length() + SEPARATOR.length() + addition.value.length();
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
        if (!(other instanceof Comment otherComment)) {
            return false;
        }

        return value.equals(otherComment.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
