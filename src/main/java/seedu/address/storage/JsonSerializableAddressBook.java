package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.VersionedAddressBook;
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
    public static final String MESSAGE_DUPLICATE_EMAIL = "Persons list contains duplicate email(s).";
    public static final String MESSAGE_DUPLICATE_GROUP = "Tutorial groups list contains duplicate group(s).";
    public static final String MESSAGE_DUPLICATE_SESSION = "Sessions list contains duplicate session(s).";
    public static final String MESSAGE_SESSION_GROUP_NOT_FOUND =
            "A session refers to a tutorial group that is not registered.";
    public static final String MESSAGE_UNKNOWN_SESSION =
            "Attendance records refer to a session that is not in the sessions list.";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private List<JsonSerializableAddressBook> historyStates;
    private List<String> historyCommands;
    private Integer historyPointer;

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();
    private final List<JsonAdaptedSession> sessions = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<String> groups,
            List<JsonAdaptedSession> sessions) {
        this(persons, groups, sessions, null, null, null);
    }

    /** Creates a saved roster with optional undo/redo history from JSON. */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("groups") List<String> groups,
            @JsonProperty("sessions") List<JsonAdaptedSession> sessions,
            @JsonProperty("historyStates") List<JsonSerializableAddressBook> historyStates,
            @JsonProperty("historyCommands") List<String> historyCommands,
            @JsonProperty("historyPointer") Integer historyPointer) {
        this.historyStates = historyStates;
        this.historyCommands = historyCommands;
        this.historyPointer = historyPointer;
        if (persons != null) {
            this.persons.addAll(persons);
        }
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
        if (source instanceof VersionedAddressBook versioned) {
            historyStates = versioned.getHistoryStates().stream().map(JsonSerializableAddressBook::new).toList();
            historyCommands = versioned.getHistoryCommands();
            historyPointer = versioned.getHistoryPointer();
        }
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        groups.addAll(source.getGroupList().stream().map(Group::toString).collect(Collectors.toList()));
        sessions.addAll(source.getSessionList().stream().map(JsonAdaptedSession::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (String groupCode : groups) {
            if (!Group.isValidGroup(groupCode)) {
                throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
            }
            Group group = new Group(groupCode);
            if (addressBook.hasGroup(group)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_GROUP);
            }
            addressBook.addGroup(group);
        }
        for (JsonAdaptedSession jsonAdaptedSession : sessions) {
            Session session = jsonAdaptedSession.toModelType();
            if (!addressBook.hasGroup(session.getGroup())) {
                throw new IllegalValueException(MESSAGE_SESSION_GROUP_NOT_FOUND);
            }
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
            if (addressBook.getPersonList().stream().anyMatch(person::hasSameEmail)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_EMAIL);
            }
            if (!areSessionsStored(person, addressBook)) {
                throw new IllegalValueException(MESSAGE_UNKNOWN_SESSION);
            }
            // Data files saved before groups were stored, such as the saved sample data, list students
            // without their groups. Registering the group keeps those files loadable.
            if (!addressBook.hasGroup(person.getGroup())) {
                logger.info("Registered tutorial group " + person.getGroup() + ", which " + person.getMatric()
                        + " belongs to but the groups list in the data file does not include");
                addressBook.addGroup(person.getGroup());
            }
            addressBook.addPerson(person);
        }
        assert addressBook.getPersonList().stream().allMatch(person -> person.getAttendances().stream()
                .allMatch(attendance -> attendance.getSession().getGroup().equals(person.getGroup())
                        && addressBook.hasSession(attendance.getSession())))
                : "Every loaded attendance record should be for a stored session of the student's own group";
        return addressBook;
    }

    /** Loads history when it is valid and matches the saved roster; otherwise keeps the roster alone. */
    public VersionedAddressBook toVersionedModelType() throws IllegalValueException {
        AddressBook current = toModelType();
        VersionedAddressBook result = new VersionedAddressBook(current);
        if (historyStates == null && historyCommands == null && historyPointer == null) {
            return result;
        }
        try {
            if (historyStates == null || historyCommands == null || historyPointer == null) {
                throw new IllegalArgumentException("Incomplete undo/redo history");
            }
            List<AddressBook> states = new ArrayList<>();
            for (JsonSerializableAddressBook state : historyStates) {
                if (state == null) {
                    throw new IllegalArgumentException("Missing history snapshot");
                }
                states.add(state.toModelType());
            }
            VersionedAddressBook restored = new VersionedAddressBook(current);
            restored.restoreHistory(states, historyCommands, historyPointer);
            if (!current.equals(restored)) {
                throw new IllegalArgumentException("History does not match the saved roster");
            }
            return restored;
        } catch (IllegalArgumentException | IllegalValueException e) {
            logger.warning("Discarding invalid undo/redo history: " + e.getMessage());
            return result;
        }
    }

    /**
     * Returns true if every attendance record of {@code person} refers to a session stored in {@code addressBook}.
     * The sessions must be loaded into {@code addressBook} before the persons.
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
