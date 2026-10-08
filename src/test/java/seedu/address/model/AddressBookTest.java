package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicateGroupException;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void addGroup_duplicateGroup_throwsDuplicateGroupException() {
        Group group = new Group("T09");
        addressBook.addGroup(group);

        assertThrows(DuplicateGroupException.class, () ->
                addressBook.addGroup(new Group("T09")));
    }

    @Test
    public void addSession_sameGroupAndWeek_throwsDuplicateSessionException() {
        Group group = new Group("T09");
        addressBook.addGroup(group);
        addressBook.addSession(new Session(group, new Week(5), new SessionDate("2026-09-15")));

        assertThrows(seedu.address.model.session.exceptions.DuplicateSessionException.class, () ->
                addressBook.addSession(new Session(group, new Week(5), new SessionDate("2026-09-16"))));
    }

    @Test
    public void addSession_outOfOrder_sortsByWeek() {
        Group group = new Group("T09");
        Session weekFive = new Session(group, new Week(5), new SessionDate("2026-09-15"));
        Session weekSix = new Session(group, new Week(6), new SessionDate("2026-09-22"));

        addressBook.addGroup(group);
        addressBook.addSession(weekSix);
        addressBook.addSession(weekFive);

        assertEquals(List.of(weekFive, weekSix), addressBook.getSessionList());
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void hasSession_sessionNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasSession(T09_WEEK_1));
    }

    @Test
    public void addSession_sessionAdded_hasAndFindsSession() {
        addressBook.addGroup(T09_WEEK_1.getGroup());
        addressBook.addSession(T09_WEEK_1);
        assertTrue(addressBook.hasSession(T09_WEEK_1));
        assertEquals(Optional.of(T09_WEEK_1), addressBook.findSession(new Group("T09"), new Week(1)));
        assertEquals(Optional.empty(), addressBook.findSession(new Group("T09"), new Week(2)));
    }

    @Test
    public void resetData_withSessions_copiesSessions() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData.getSessionList(), addressBook.getSessionList());
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", groups=" + addressBook.getGroupList() + ", sessions=" + addressBook.getSessionList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Group> getGroupList() {
            return FXCollections.observableArrayList();
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Session> getSessionList() {
            return FXCollections.observableArrayList();
        }
    }

}
