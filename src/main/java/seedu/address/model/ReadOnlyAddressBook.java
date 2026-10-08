package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns an unmodifiable view of the registered tutorial groups.
     */
    ObservableList<Group> getGroupList();

    /**
     * Returns an unmodifiable view of the tutorial sessions, sorted by group and week.
     */
    ObservableList<Session> getSessionList();

}
