package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

/**
 * Represents the calendar date of a tutorial session.
 */
public class SessionDate {

    public static final String MESSAGE_CONSTRAINTS = "Session date should be a real calendar date in YYYY-MM-DD "
            + "format.";
    private static final String VALIDATION_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public final LocalDate value;

    /**
     * Constructs a {@code SessionDate}.
     */
    public SessionDate(String date) {
        requireNonNull(date);
        String trimmedDate = date.trim();
        checkArgument(isValidSessionDate(trimmedDate), MESSAGE_CONSTRAINTS);
        value = LocalDate.parse(trimmedDate, FORMATTER);
    }

    /**
     * Returns true if {@code test} is a real calendar date in YYYY-MM-DD format.
     */
    public static boolean isValidSessionDate(String test) {
        if (test == null || !test.trim().matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            LocalDate.parse(test.trim(), FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return FORMATTER.format(value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof SessionDate otherDate && value.equals(otherDate.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
