package seedu.address.model.attendance;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.session.Session;

/**
 * Represents whether a student was present or absent at one session.
 * Each attendance is owned by the student it describes.
 * Guarantees: details are present and not null, immutable.
 */
public class Attendance {

    private final Session session;
    private final Status status;

    /**
     * Every field must be present and not null.
     */
    public Attendance(Session session, Status status) {
        requireAllNonNull(session, status);
        this.session = session;
        this.status = status;
    }

    public Session getSession() {
        return session;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Returns true if this attendance is recorded for {@code otherSession}, whatever its status.
     */
    public boolean isForSession(Session otherSession) {
        return session.isSameSession(otherSession);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Attendance otherAttendance)) {
            return false;
        }

        return session.equals(otherAttendance.session)
                && status == otherAttendance.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("session", session)
                .add("status", status)
                .toString();
    }

}
