package seedu.address.model.session;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Group;

/**
 * Represents a meeting of a tutorial group in one teaching week.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Session {

    // Identity fields
    private final Group group;
    private final Week week;

    // Data fields
    private final SessionDate date;

    /**
     * Every field must be present and not null.
     */
    public Session(Group group, Week week, SessionDate date) {
        requireAllNonNull(group, week, date);
        this.group = group;
        this.week = week;
        this.date = date;
    }

    public Group getGroup() {
        return group;
    }

    public Week getWeek() {
        return week;
    }

    public SessionDate getDate() {
        return date;
    }

    /**
     * Returns true if both sessions belong to the same group and week.
     * The date is not compared, as a group meets at most once per teaching week.
     * This defines a weaker notion of equality between two sessions.
     */
    public boolean isSameSession(Session otherSession) {
        if (otherSession == this) {
            return true;
        }

        return otherSession != null
                && otherSession.group.equals(group)
                && otherSession.week.equals(week);
    }

    /**
     * Returns true if this session belongs to {@code group} and falls in {@code week}.
     */
    public boolean isFor(Group group, Week week) {
        return this.group.equals(group) && this.week.equals(week);
    }

    /**
     * Returns true if both sessions have the same group, week and date.
     * This defines a stronger notion of equality between two sessions.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Session otherSession)) {
            return false;
        }

        return group.equals(otherSession.group)
                && week.equals(otherSession.week)
                && date.equals(otherSession.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, week, date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("group", group)
                .add("week", week)
                .add("date", date)
                .toString();
    }

}
