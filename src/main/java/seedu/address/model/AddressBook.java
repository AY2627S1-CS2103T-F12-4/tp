package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.group.Group;
import seedu.address.model.group.exceptions.DuplicateGroupException;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;
import seedu.address.model.session.exceptions.DuplicateSessionException;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    private final ObservableList<Session> sessions = FXCollections.observableArrayList();
    private final ObservableList<Group> unmodifiableGroups = FXCollections.unmodifiableObservableList(groups);
    private final ObservableList<Session> unmodifiableSessions = FXCollections.unmodifiableObservableList(sessions);

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Replaces the registered tutorial groups.
     */
    public void setGroups(List<Group> groups) {
        this.groups.clear();
        for (Group group : groups) {
            addGroup(group);
        }
    }

    /**
     * Replaces the tutorial sessions.
     */
    public void setSessions(List<Session> sessions) {
        this.sessions.clear();
        for (Session session : sessions) {
            addSession(session);
        }
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setGroups(newData.getGroupList());
        setSessions(newData.getSessionList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// tutorial-group operations

    /**
     * Returns true if {@code group} is registered.
     */
    public boolean hasGroup(Group group) {
        requireNonNull(group);
        return groups.contains(group);
    }

    /**
     * Registers a tutorial group.
     */
    public void addGroup(Group group) {
        requireNonNull(group);
        if (hasGroup(group)) {
            throw new DuplicateGroupException();
        }
        groups.add(group);
    }

    //// session operations

    /**
     * Returns the session for {@code group} and {@code week}, if it exists.
     */
    public Optional<Session> getSession(Group group, Week week) {
        requireNonNull(group);
        requireNonNull(week);
        return sessions.stream()
                .filter(session -> session.getGroup().equals(group) && session.getWeek().equals(week))
                .findFirst();
    }

    /**
     * Returns true if a session with the same group and week exists.
     */
    public boolean hasSession(Session session) {
        requireNonNull(session);
        return sessions.stream().anyMatch(session::isSameSession);
    }

    /**
     * Adds a tutorial session and maintains group/week display order.
     */
    public void addSession(Session session) {
        requireNonNull(session);
        if (!hasGroup(session.getGroup())) {
            throw new IllegalArgumentException("Session tutorial group must be registered");
        }
        if (hasSession(session)) {
            throw new DuplicateSessionException();
        }
        sessions.add(session);
        FXCollections.sort(sessions, (first, second) -> {
            int groupComparison = first.getGroup().code.compareTo(second.getGroup().code);
            return groupComparison != 0 ? groupComparison : first.getWeek().compareTo(second.getWeek());
        });
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("groups", groups)
                .add("sessions", sessions)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Group> getGroupList() {
        return unmodifiableGroups;
    }

    @Override
    public ObservableList<Session> getSessionList() {
        return unmodifiableSessions;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && groups.equals(otherAddressBook.groups)
                && sessions.equals(otherAddressBook.sessions);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(persons, groups, sessions);
    }
}
