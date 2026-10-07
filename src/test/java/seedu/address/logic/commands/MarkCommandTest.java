package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;
import static seedu.address.testutil.TypicalSessions.getTypicalSessions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;
import seedu.address.testutil.ModelStub;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code MarkCommand}.
 * In the typical address book, ALICE (index 1) to CARL are in T09 and DANIEL (index 4) onwards are in T10;
 * the typical sessions are added to it, so T09 has sessions in weeks 1 and 2, and T10 only in week 1.
 * The tests with a stub model check only the command's own behavior, without a real model.
 */
public class MarkCommandTest {

    private static final Index INDEX_FIFTH_PERSON = Index.fromOneBased(5);

    private final Model model = new ModelManager(getTypicalAddressBookWithSessions(), new UserPrefs());

    /**
     * Returns the typical address book with the typical sessions added.
     */
    private static AddressBook getTypicalAddressBookWithSessions() {
        AddressBook addressBook = getTypicalAddressBook();
        getTypicalSessions().forEach(addressBook::addSession);
        return addressBook;
    }

    @Test
    public void execute_firstMarkUnfilteredList_success() {
        Person alice = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person markedAlice = new PersonBuilder(alice).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT);

        String expectedMessage = "Marked Alice Pauline as PRESENT for T09 week 1.";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(alice, markedAlice);

        assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_remark_overwritesAndNamesPreviousStatus() {
        Person alice = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person alicePresent = new PersonBuilder(alice).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        model.setPerson(alice, alicePresent);

        Person aliceAbsent = new PersonBuilder(alice).withAttendance(T09_WEEK_1, Status.ABSENT).build();
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.ABSENT);

        String expectedMessage = "Marked Alice Pauline as ABSENT for T09 week 1 (was PRESENT).";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(alicePresent, aliceAbsent);

        assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentInAnotherGroup_usesOwnGroupSession() {
        Person elle = model.getFilteredPersonList().get(INDEX_FIFTH_PERSON.getZeroBased());
        Person markedElle = new PersonBuilder(elle).withAttendance(T10_WEEK_1, Status.ABSENT).build();
        MarkCommand markCommand = new MarkCommand(INDEX_FIFTH_PERSON, new Week(1), Status.ABSENT);

        String expectedMessage = "Marked Elle Meyer as ABSENT for T10 week 1.";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(elle, markedElle);

        assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noSessionForWeek_failure() {
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(3), Status.PRESENT);
        String expectedMessage =
                "T09 has no session for week 3. Create it first with: session grp/T09 w/3 d/YYYY-MM-DD";
        assertCommandFailure(markCommand, model, expectedMessage);
    }

    @Test
    public void execute_sessionOnlyInOtherGroup_failure() {
        // T09 has a week 2 session, but ELLE is in T10, which does not
        MarkCommand markCommand = new MarkCommand(INDEX_FIFTH_PERSON, new Week(2), Status.PRESENT);
        String expectedMessage =
                "T10 has no session for week 2. Create it first with: session grp/T10 w/2 d/YYYY-MM-DD";
        assertCommandFailure(markCommand, model, expectedMessage);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        MarkCommand markCommand = new MarkCommand(outOfBoundIndex, new Week(1), Status.PRESENT);

        assertCommandFailure(markCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_successAndFilterKept() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        Person benson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person markedBenson = new PersonBuilder(benson).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT);

        String expectedMessage = "Marked Benson Meier as PRESENT for T09 week 1.";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(benson, markedBenson);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    /**
     * Mark filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        MarkCommand markCommand = new MarkCommand(outOfBoundIndex, new Week(1), Status.PRESENT);

        assertCommandFailure(markCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_firstMarkWithStubModel_recordsAttendanceForThatStudentOnly() throws Exception {
        ModelStubWithStudentsAndSessions modelStub =
                new ModelStubWithStudentsAndSessions(List.of(ALICE, BENSON), List.of(T09_WEEK_1));

        CommandResult result = new MarkCommand(INDEX_SECOND_PERSON, new Week(1), Status.PRESENT).execute(modelStub);

        assertEquals("Marked Benson Meier as PRESENT for T09 week 1.", result.getFeedbackToUser());
        assertEquals(List.of(BENSON), modelStub.replacedPersons);
        assertEquals(List.of(new PersonBuilder(BENSON).withAttendance(T09_WEEK_1, Status.PRESENT).build()),
                modelStub.replacementPersons);
    }

    @Test
    public void execute_remarkWithStubModel_replacesRecordAndNamesPreviousStatus() throws Exception {
        Person bensonPresent = new PersonBuilder(BENSON).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        ModelStubWithStudentsAndSessions modelStub =
                new ModelStubWithStudentsAndSessions(List.of(bensonPresent), List.of(T09_WEEK_1));

        CommandResult result = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.ABSENT).execute(modelStub);

        assertEquals("Marked Benson Meier as ABSENT for T09 week 1 (was PRESENT).", result.getFeedbackToUser());
        Person bensonAbsent = modelStub.replacementPersons.get(0);
        assertEquals(Set.of(new Attendance(T09_WEEK_1, Status.ABSENT)), bensonAbsent.getAttendances());
    }

    @Test
    public void execute_listSpanningGroupsWithStubModel_usesEachStudentsOwnGroup() throws Exception {
        // ALICE is in T09 and ELLE in T10, and both groups have a week 1 session
        ModelStubWithStudentsAndSessions modelStub =
                new ModelStubWithStudentsAndSessions(List.of(ALICE, ELLE), List.of(T09_WEEK_1, T10_WEEK_1));

        new MarkCommand(INDEX_SECOND_PERSON, new Week(1), Status.PRESENT).execute(modelStub);

        assertEquals(Set.of(new Attendance(T10_WEEK_1, Status.PRESENT)),
                modelStub.replacementPersons.get(0).getAttendances());
    }

    @Test
    public void execute_noSessionForOwnGroupWithStubModel_throwsCommandExceptionAndChangesNothing() {
        // Only T09 has a week 1 session, but ELLE is in T10
        ModelStubWithStudentsAndSessions modelStub =
                new ModelStubWithStudentsAndSessions(List.of(ELLE), List.of(T09_WEEK_1));
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT);

        assertThrows(CommandException.class,
                "T10 has no session for week 1. Create it first with: session grp/T10 w/1 d/YYYY-MM-DD", () ->
                markCommand.execute(modelStub));
        assertTrue(modelStub.replacedPersons.isEmpty());
    }

    @Test
    public void execute_indexOutOfRangeWithStubModel_throwsCommandExceptionAndChangesNothing() {
        ModelStubWithStudentsAndSessions modelStub =
                new ModelStubWithStudentsAndSessions(List.of(ALICE), List.of(T09_WEEK_1));
        MarkCommand markCommand = new MarkCommand(INDEX_SECOND_PERSON, new Week(1), Status.PRESENT);

        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                markCommand.execute(modelStub));
        assertTrue(modelStub.replacedPersons.isEmpty());
    }

    @Test
    public void equals() {
        final MarkCommand standardCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT);

        // same values -> returns true
        assertTrue(standardCommand.equals(new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.PRESENT)));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new MarkCommand(INDEX_SECOND_PERSON, new Week(1), Status.PRESENT)));

        // different week -> returns false
        assertFalse(standardCommand.equals(new MarkCommand(INDEX_FIRST_PERSON, new Week(2), Status.PRESENT)));

        // different status -> returns false
        assertFalse(standardCommand.equals(new MarkCommand(INDEX_FIRST_PERSON, new Week(1), Status.ABSENT)));
    }

    @Test
    public void toStringMethod() {
        MarkCommand markCommand = new MarkCommand(INDEX_FIRST_PERSON, new Week(5), Status.ABSENT);
        String expected = MarkCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", week=5, status=ABSENT}";
        assertEquals(expected, markCommand.toString());
    }

    /**
     * A Model stub that displays a fixed list of persons, holds a fixed list of sessions, and records the persons
     * replaced in it. Any other call to the model, including changing the displayed list's filter, fails the test.
     */
    private static class ModelStubWithStudentsAndSessions extends ModelStub {
        private final ObservableList<Person> displayedPersons;
        private final List<Session> sessions;
        private final List<Person> replacedPersons = new ArrayList<>();
        private final List<Person> replacementPersons = new ArrayList<>();

        ModelStubWithStudentsAndSessions(List<Person> displayedPersons, List<Session> sessions) {
            this.displayedPersons = FXCollections.observableArrayList(displayedPersons);
            this.sessions = sessions;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.unmodifiableObservableList(displayedPersons);
        }

        @Override
        public Optional<Session> findSession(Group group, Week week) {
            return sessions.stream().filter(session -> session.isFor(group, week)).findFirst();
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            requireAllNonNull(target, editedPerson);
            replacedPersons.add(target);
            replacementPersons.add(editedPerson);
        }
    }
}
