package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private static final Comparator<Person> MATRIC_ORDER = Comparator.comparing(person -> person.getMatric().value);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> displayedPersons;
    private final ReadOnlyObjectWrapper<Group> activeGroup = new ReadOnlyObjectWrapper<>();

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        displayedPersons = new SortedList<>(filteredPersons, MATRIC_ORDER);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Tutorial Groups and Sessions ===============================================================

    @Override
    public boolean hasGroup(Group group) {
        requireNonNull(group);
        return addressBook.hasGroup(group);
    }

    @Override
    public void addGroup(Group group) {
        addressBook.addGroup(group);
    }

    @Override
    public ObservableList<Group> getGroupList() {
        return addressBook.getGroupList();
    }

    @Override
    public void setActiveGroup(Group group) {
        requireNonNull(group);
        if (!hasGroup(group)) {
            throw new IllegalArgumentException("Active tutorial group must be registered");
        }
        activeGroup.set(group);
    }

    @Override
    public Optional<Group> getActiveGroup() {
        return Optional.ofNullable(activeGroup.get());
    }

    @Override
    public Optional<Session> findSession(Group group, Week week) {
        return addressBook.findSession(group, week);
    }

    @Override
    public void addSession(Session session) {
        requireNonNull(session);
        addressBook.addSession(session);
    }

    @Override
    public ObservableList<Session> getSessionList() {
        return addressBook.getSessionList();
    }

    @Override
    public boolean hasSession(Session session) {
        return addressBook.hasSession(session);
    }

    @Override
    public void showGroup(Group group) {
        setActiveGroup(group);
        filteredPersons.setPredicate(person -> person.getGroup().equals(group));
        displayedPersons.setComparator(MATRIC_ORDER);
    }

    @Override
    public ReadOnlyObjectProperty<Group> activeGroupProperty() {
        return activeGroup.getReadOnlyProperty();
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return displayedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        updateFilteredPersonList(predicate, MATRIC_ORDER);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
        displayedPersons.setComparator(comparator);
        activeGroup.set(null);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && Objects.equals(activeGroup.get(), otherModelManager.activeGroup.get())
                && displayedPersons.equals(otherModelManager.displayedPersons);
    }

}
