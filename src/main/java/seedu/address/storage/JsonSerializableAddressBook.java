package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.attendance.Attendance;
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

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedSession> sessions = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and sessions.
     * Data files saved before sessions were added have no sessions list, which is read as an empty one.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("sessions") List<JsonAdaptedSession> sessions) {
        this.persons.addAll(persons);
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
