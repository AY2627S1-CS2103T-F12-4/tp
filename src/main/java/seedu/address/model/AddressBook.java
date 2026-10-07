package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.session.Session;
import seedu.address.model.session.UniqueSessionList;
import seedu.address.model.session.Week;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison for persons, and .isSameSession for sessions).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueSessionList sessions = new UniqueSessionList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons and Sessions in the {@code toBeCopied}
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
        persons.forEach(person -> registerReferencedGroup(person.getGroup()));
    }

    /**
     * Replaces the contents of the session list with {@code sessions}.
     * {@code sessions} must not contain duplicate sessions.
     */
    public void setSessions(List<Session> sessions) {
        this.sessions.setSessions(sessions);
        sessions.forEach(session -> registerReferencedGroup(session.getGroup()));
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        groups.setAll(newData.getGroupList());
        setPersons(newData.getPersonList());
        setSessions(newData.getSessionList());
    }

    /**
     * Registers an empty tutorial group, independently of students and sessions.
     *
     * @throws IllegalArgumentException if the group is already registered.
     */
    public void addGroup(Group group) {
        requireNonNull(group);
        if (hasGroup(group)) {
            throw new IllegalArgumentException("This tutorial group already exists: " + group);
        }
        groups.add(group);
    }

    /**
     * Returns whether a group has been registered, even when it has no students or sessions.
     */
    public boolean hasGroup(Group group) {
        requireNonNull(group);
        return groups.contains(group);
    }

    // Keeps the existing add/session model APIs compatible with pre-registry callers.
    // Command-level checks for creating groups belong to the init/add/session features.
    private void registerReferencedGroup(Group group) {
        if (!hasGroup(group)) {
            groups.add(group);
        }
    }

    @Override
    public ObservableList<Group> getGroupList() {
        return FXCollections.unmodifiableObservableList(groups);
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
        registerReferencedGroup(p.getGroup());
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        registerReferencedGroup(editedPerson.getGroup());
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// session-level operations

    /**
     * Returns true if the group of {@code session} already has a session in the same week.
     */
    public boolean hasSession(Session session) {
        requireNonNull(session);
        return sessions.contains(session);
    }

    /**
     * Adds a session to the address book.
     * The group must not already have a session in the same week.
     */
    public void addSession(Session session) {
        sessions.add(session);
        registerReferencedGroup(session.getGroup());
    }

    /**
     * Returns the session of {@code group} in {@code week}, if there is one.
     */
    public Optional<Session> findSession(Group group, Week week) {
        return sessions.find(group, week);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("sessions", sessions)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Session> getSessionList() {
        return sessions.asUnmodifiableObservableList();
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

        return new HashSet<>(groups).equals(new HashSet<>(otherAddressBook.groups))
                && persons.equals(otherAddressBook.persons)
                && sessions.equals(otherAddressBook.sessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new HashSet<>(groups), persons, sessions);
    }
}
