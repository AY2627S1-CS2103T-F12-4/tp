package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_GROUP = "Tutorial groups list contains duplicate group(s).";
    public static final String MESSAGE_DUPLICATE_SESSION = "Sessions list contains duplicate session(s).";
    public static final String MESSAGE_SESSION_GROUP_NOT_FOUND =
            "A session refers to a tutorial group that is not registered.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();
    private final List<JsonAdaptedSession> sessions = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("groups") List<String> groups,
            @JsonProperty("sessions") List<JsonAdaptedSession> sessions) {
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
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
