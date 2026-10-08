package seedu.address.model.session;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Group;

/**
 * Represents a tutorial session, uniquely identified by its group and teaching week.
 */
public class Session {

    private final Group group;
    private final Week week;
    private final SessionDate date;

    /**
     * Constructs a {@code Session}.
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
     * Returns true if both sessions belong to the same group and teaching week.
     */
    public boolean isSameSession(Session otherSession) {
        return otherSession != null
                && group.equals(otherSession.group)
                && week.equals(otherSession.week);
    }

    /**
     * Returns true if this session belongs to {@code group} and falls in {@code week}.
     */
    public boolean isFor(Group group, Week week) {
        return this.group.equals(group) && this.week.equals(week);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("group", group)
                .add("week", week)
                .add("date", date)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Session otherSession
                && group.equals(otherSession.group)
                && week.equals(otherSession.week)
                && date.equals(otherSession.date));
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, week, date);
    }
}
