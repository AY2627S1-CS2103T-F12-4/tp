package seedu.address.testutil;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

/**
 * A default model stub that has all of the methods failing.
 * Command tests extend it and override only the methods the command under test is expected to call,
 * so that any other call to the model fails the test.
 */
public class ModelStub implements Model {
    @Override
    public void showGroup(Group group) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void addGroup(Group group) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public boolean hasGroup(Group group) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ReadOnlyObjectProperty<Group> activeGroupProperty() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public GuiSettings getGuiSettings() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void addPerson(Person person) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void setAddressBook(ReadOnlyAddressBook newData) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ObservableList<Group> getGroupList() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void setActiveGroup(Group group) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public Optional<Group> getActiveGroup() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ObservableList<Session> getSessionList() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public boolean hasPerson(Person person) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void deletePerson(Person target) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public boolean hasSession(Session session) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public void addSession(Session session) {
        throw new AssertionError("This method should not be called.");
    }

    @Override
    public Optional<Session> findSession(Group group, Week week) {
        throw new AssertionError("This method should not be called.");
    }
}
