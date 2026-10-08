package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

/**
 * Represents a teaching week from 1 to 13.
 */
public class Week implements Comparable<Week> {

    public static final int FIRST_WEEK = 1;
    public static final int LAST_WEEK = 13;
    public static final String MESSAGE_CONSTRAINTS =
            "Week should be a whole number from " + FIRST_WEEK + " to " + LAST_WEEK + ".";
    private static final String VALIDATION_REGEX = "[1-9]\\d?";

    public final int value;

    /**
     * Constructs a {@code Week}.
     */
    public Week(int value) {
        checkArgument(isValidWeek(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    /**
     * Returns true if {@code value} is a valid teaching week.
     */
    public static boolean isValidWeek(int value) {
        return value >= FIRST_WEEK && value <= LAST_WEEK;
    }

    /**
     * Returns true if {@code value} is a valid teaching week without a leading zero.
     */
    public static boolean isValidWeek(String value) {
        requireNonNull(value);
        return value.matches(VALIDATION_REGEX) && isValidWeek(Integer.parseInt(value));
    }

    @Override
    public int compareTo(Week other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Week otherWeek && value == otherWeek.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
