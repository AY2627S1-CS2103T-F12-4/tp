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

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Session's %s field is missing!";

    private final String group;
    private final String week;
    private final String date;

    /**
     * Constructs a {@code JsonAdaptedSession} with the given session details.
     */
    @JsonCreator
    public JsonAdaptedSession(@JsonProperty("group") String group, @JsonProperty("week") String week,
            @JsonProperty("date") String date) {
        this.group = group;
        this.week = week;
        this.date = date;
    }

    /**
     * Converts a given {@code Session} into this class for Jackson use.
     */
    public JsonAdaptedSession(Session source) {
        group = source.getGroup().value;
        week = source.getWeek().toString();
        date = source.getDate().toString();
    }

    /**
     * Converts this Jackson-friendly adapted session object into the model's {@code Session} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted session.
     */
    public Session toModelType() throws IllegalValueException {
        if (group == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Group.class.getSimpleName()));
        }
        if (!Group.isValidGroup(group)) {
            throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
        }
        final Group modelGroup = new Group(group);

        if (week == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Week.class.getSimpleName()));
        }
        if (!Week.isValidWeek(week)) {
            throw new IllegalValueException(Week.MESSAGE_CONSTRAINTS);
        }
        final Week modelWeek = new Week(Integer.parseInt(week));

        if (date == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, SessionDate.class.getSimpleName()));
        }
        if (!SessionDate.isValidSessionDate(date)) {
            throw new IllegalValueException(SessionDate.MESSAGE_CONSTRAINTS);
        }
        final SessionDate modelDate = new SessionDate(date);

        return new Session(modelGroup, modelWeek, modelDate);
    }

}
