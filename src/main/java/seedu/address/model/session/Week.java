package seedu.address.model.session;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

/**
 * Represents a teaching week from 1 to 13.
 */
public class Week implements Comparable<Week> {

    public static final String MESSAGE_CONSTRAINTS = "Week should be an integer from 1 to 13.";

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
        return value >= 1 && value <= 13;
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
