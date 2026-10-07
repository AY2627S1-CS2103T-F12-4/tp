package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents the calendar date on which a session took place.
 * Guarantees: immutable; is valid as declared in {@link #isValidSessionDate(String)}
 */
public class SessionDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Dates should be real calendar dates in the format YYYY-MM-DD, e.g. 2026-09-15.";

    // STRICT rejects dates that do not exist, such as 2026-02-30, instead of adjusting them
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public final LocalDate value;

    /**
     * Constructs a {@code SessionDate}.
     *
     * @param date A valid date in the format YYYY-MM-DD.
     */
    public SessionDate(String date) {
        requireNonNull(date);
        checkArgument(isValidSessionDate(date), MESSAGE_CONSTRAINTS);
        value = LocalDate.parse(date, FORMATTER);
    }

    /**
     * Returns true if a given string is a real calendar date in the format YYYY-MM-DD.
     */
    public static boolean isValidSessionDate(String test) {
        requireNonNull(test);
        try {
            LocalDate.parse(test, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SessionDate otherDate)) {
            return false;
        }

        return value.equals(otherDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
