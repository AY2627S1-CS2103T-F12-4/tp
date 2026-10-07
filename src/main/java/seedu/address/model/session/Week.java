package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a teaching week of the semester.
 * Guarantees: immutable; is valid as declared in {@link #isValidWeek(int)}
 */
public class Week {

    public static final int FIRST_WEEK = 1;
    public static final int LAST_WEEK = 13;

    public static final String MESSAGE_CONSTRAINTS =
            "Week should be a whole number from " + FIRST_WEEK + " to " + LAST_WEEK + ".";

    /*
     * One or two digits without a leading zero. The range is checked separately.
     */
    private static final String VALIDATION_REGEX = "[1-9]\\d?";

    public final int value;

    /**
     * Constructs a {@code Week}.
     *
     * @param week A valid week number.
     */
    public Week(int week) {
        checkArgument(isValidWeek(week), MESSAGE_CONSTRAINTS);
        value = week;
    }

    /**
     * Returns true if a given number is a valid teaching week.
     */
    public static boolean isValidWeek(int test) {
        return test >= FIRST_WEEK && test <= LAST_WEEK;
    }

    /**
     * Returns true if a given string is a valid teaching week, written without leading zeros.
     */
    public static boolean isValidWeek(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX) && isValidWeek(Integer.parseInt(test));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Week otherWeek)) {
            return false;
        }

        return value == otherWeek.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
