package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_SESSION = "Sessions list contains duplicate session(s).";
    public static final String MESSAGE_UNKNOWN_SESSION =
            "Attendance records refer to a session that is not in the sessions list.";

    private final List<String> groups = new ArrayList<>();

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedSession> sessions = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons, sessions and group registry.
     * Missing session lists are read as empty. Missing group registries are reconstructed from existing records.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("sessions") List<JsonAdaptedSession> sessions,
            @JsonProperty("groups") List<String> groups) {
        this.persons.addAll(persons);
        if (groups != null) {
            this.groups.addAll(groups);
        }
        if (sessions != null) {
            this.sessions.addAll(sessions);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        groups.addAll(source.getGroupList().stream().map(group -> group.value).toList());
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        sessions.addAll(source.getSessionList().stream().map(JsonAdaptedSession::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (String value : groups) {
            if (value == null || !Group.isValidGroup(value.trim().toUpperCase(Locale.ROOT))) {
                throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
            }
            Group group = new Group(value.trim().toUpperCase(Locale.ROOT));
            if (addressBook.hasGroup(group)) {
                throw new IllegalValueException("Groups list contains duplicate tutorial groups.");
            }
            addressBook.addGroup(group);
        }
        // Older data has no registry; adding its sessions/students retains their groups.
        // Sessions are added first, so that each attendance record can be checked against them
        for (JsonAdaptedSession jsonAdaptedSession : sessions) {
            Session session = jsonAdaptedSession.toModelType();
            if (addressBook.hasSession(session)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_SESSION);
            }
            addressBook.addSession(session);
        }

        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (!areSessionsStored(person, addressBook)) {
                throw new IllegalValueException(MESSAGE_UNKNOWN_SESSION);
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

    /**
     * Returns true if every attendance record of {@code person} refers to a session stored in {@code addressBook}.
     */
    private static boolean areSessionsStored(Person person, AddressBook addressBook) {
        for (Attendance attendance : person.getAttendances()) {
            Session session = attendance.getSession();
            Optional<Session> storedSession = addressBook.findSession(session.getGroup(), session.getWeek());
            if (!storedSession.map(session::equals).orElse(false)) {
                return false;
            }
        }
        return true;
    }

}
