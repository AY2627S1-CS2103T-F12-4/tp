package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;
import static seedu.address.testutil.TypicalSessions.getTypicalSessions;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code MarkCommand}.
 * In the typical address book, ALICE (index 1) to CARL are in T09 and DANIEL (index 4) onwards are in T10;
 * the typical sessions are added to it, so T09 has sessions in weeks 1 and 2, and T10 only in week 1.
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
}
