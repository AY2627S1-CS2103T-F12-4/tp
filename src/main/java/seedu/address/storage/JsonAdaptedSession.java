package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

/**
 * Jackson-friendly version of {@link Session}.
 */
class JsonAdaptedSession {

    private final String group;
    private final String week;
    private final String date;

    /**
     * Constructs a {@code JsonAdaptedSession} with the given details.
     */
    @JsonCreator
    public JsonAdaptedSession(@JsonProperty("group") String group,
            @JsonProperty("week") String week, @JsonProperty("date") String date) {
        this.group = group;
        this.week = week;
        this.date = date;
    }

    /**
     * Converts a source {@code Session} into its JSON-friendly representation.
     */
    public JsonAdaptedSession(Session source) {
        group = source.getGroup().toString();
        week = source.getWeek().toString();
        date = source.getDate().toString();
    }

    public String getGroup() {
        return group;
    }

    public String getWeek() {
        return week;
    }

    public String getDate() {
        return date;
    }

    /**
     * Converts this JSON-friendly session into the model type.
     */
    public Session toModelType() throws IllegalValueException {
        if (!Group.isValidGroup(group)) {
            throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
        }

        int weekNumber;
        try {
            weekNumber = Integer.parseInt(week);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalValueException(Week.MESSAGE_CONSTRAINTS);
        }
        if (!Week.isValidWeek(weekNumber)) {
            throw new IllegalValueException(Week.MESSAGE_CONSTRAINTS);
        }

        if (!SessionDate.isValidSessionDate(date)) {
            throw new IllegalValueException(SessionDate.MESSAGE_CONSTRAINTS);
        }

        return new Session(new Group(group), new Week(weekNumber), new SessionDate(date));
    }
}
