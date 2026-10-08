package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.session.Session;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_INVALID_ATTENDANCES =
            "A person can only have attendance records for sessions of their own group, at most one per session.";

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final Matric matric;

    // Data fields
    private final Address address;
    private final Group group;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Attendance> attendances = new HashSet<>();

    /**
     * Creates a person with no attendance records.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Matric matric, Group group,
            Set<Tag> tags) {
        this(name, phone, email, address, matric, group, tags, Collections.emptySet());
    }

    /**
     * Every field must be present and not null.
     * {@code attendances} must satisfy {@link #areValidAttendances(Group, Collection)}.
     */
    public Person(Name name, Phone phone, Email email, Address address, Matric matric, Group group,
            Set<Tag> tags, Set<Attendance> attendances) {
        requireAllNonNull(name, phone, email, address, matric, group, tags, attendances);
        checkArgument(areValidAttendances(group, attendances), MESSAGE_INVALID_ATTENDANCES);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.matric = matric;
        this.group = group;
        this.tags.addAll(tags);
        this.attendances.addAll(attendances);
    }

    /**
     * Returns true if every attendance in {@code attendances} is for a session of {@code group},
     * and no two of them are for the same session.
     */
    public static boolean areValidAttendances(Group group, Collection<Attendance> attendances) {
        Set<Session> seenSessions = new HashSet<>();
        for (Attendance attendance : attendances) {
            Session session = attendance.getSession();
            boolean isSessionSeen = seenSessions.stream().anyMatch(session::isSameSession);
            if (!session.getGroup().equals(group) || isSessionSeen) {
                return false;
            }
            seenSessions.add(session);
        }
        return true;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Matric getMatric() {
        return matric;
    }

    public Group getGroup() {
        return group;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns an immutable attendance set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Attendance> getAttendances() {
        return Collections.unmodifiableSet(attendances);
    }

    /**
     * Returns this person's attendance for {@code session}, or an empty {@code Optional} if the person
     * has not been marked for it.
     */
    public Optional<Attendance> getAttendance(Session session) {
        return attendances.stream()
                .filter(attendance -> attendance.isForSession(session))
                .findFirst();
    }

    /**
     * Returns a copy of this person with {@code attendance} recorded, replacing any earlier attendance
     * for the same session.
     */
    public Person withAttendance(Attendance attendance) {
        Set<Attendance> updatedAttendances = new HashSet<>(attendances);
        updatedAttendances.removeIf(existing -> existing.isForSession(attendance.getSession()));
        updatedAttendances.add(attendance);
        return new Person(name, phone, email, address, matric, group, tags, updatedAttendances);
    }

    /**
     * Returns true if both persons have the same matriculation number.
     * A matriculation number identifies a student on its own, so two students may share a
     * name but not a matriculation number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getMatric().equals(getMatric());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && matric.equals(otherPerson.matric)
                && group.equals(otherPerson.group)
                && tags.equals(otherPerson.tags)
                && attendances.equals(otherPerson.attendances);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, matric, group, tags, attendances);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("matric", matric)
                .add("group", group)
                .add("tags", tags)
                .add("attendances", attendances)
                .toString();
    }

}
