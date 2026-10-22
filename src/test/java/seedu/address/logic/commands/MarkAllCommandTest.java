package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;
import static seedu.address.testutil.TypicalSessions.getTypicalSessions;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code MarkAllCommand}.
 * In the typical address book, ALICE to CARL are in T09 and DANIEL to GEORGE are in T10;
 * the typical sessions are added to it, so T09 has sessions in weeks 1 and 2, and T10 only in week 1.
 */
public class MarkAllCommandTest {

    private static final Group T09 = new Group("T09");

    private final Model model = new ModelManager(getTypicalAddressBookWithSessions(), new UserPrefs());

    /**
     * Returns the typical address book with the typical sessions added.
     */
    private static AddressBook getTypicalAddressBookWithSessions() {
        AddressBook addressBook = getTypicalAddressBook();
        getTypicalSessions().forEach(addressBook::addSession);
        return addressBook;
    }

    /**
     * Returns a copy of {@code model} in which every displayed student is marked with {@code status}
     * for the session of their own group in {@code sessions}.
     */
    private static Model markDisplayed(Model model, Status status, List<Session> sessions) {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        model.getActiveGroup().ifPresent(expectedModel::showGroup);
        for (Person student : List.copyOf(model.getFilteredPersonList())) {
            Session session = sessions.stream().filter(s -> s.getGroup().equals(student.getGroup()))
                    .findFirst().orElseThrow();
            expectedModel.setPerson(student, new PersonBuilder(student).withAttendance(session, status).build());
        }
        return expectedModel;
    }

    @Test
    public void execute_oneGroupDisplayed_marksEveryStudentInIt() {
        model.showGroup(T09);
        Model expectedModel = markDisplayed(model, Status.PRESENT, List.of(T09_WEEK_1));

        assertCommandSuccess(new MarkAllCommand(new Week(1), Status.PRESENT), model,
                "Marked 3 student(s) as PRESENT for week 1 in T09.", expectedModel);
    }

    @Test
    public void execute_listSpanningGroups_usesEachStudentsOwnGroupSession() {
        Model expectedModel = markDisplayed(model, Status.ABSENT, List.of(T09_WEEK_1, T10_WEEK_1));

        assertCommandSuccess(new MarkAllCommand(new Week(1), Status.ABSENT), model,
                "Marked 7 student(s) as ABSENT for week 1 in T09, T10.", expectedModel);
    }

    @Test
    public void execute_someAlreadyMarked_replacesTheirRecordsAndCountsThem() {
        model.showGroup(T09);
        Person alice = model.getFilteredPersonList().get(0);
        model.setPerson(alice, new PersonBuilder(alice).withAttendance(T09_WEEK_1, Status.ABSENT).build());
        Model expectedModel = markDisplayed(model, Status.PRESENT, List.of(T09_WEEK_1));

        assertCommandSuccess(new MarkAllCommand(new Week(1), Status.PRESENT), model,
                "Marked 3 student(s) as PRESENT for week 1 in T09 (1 already marked).", expectedModel);
    }

    @Test
    public void execute_oneGroupWithoutSession_marksNoOne() {
        // T09 has a week 2 session but T10 does not, so not even the T09 students are marked
        String expectedMessage = String.format(MarkCommand.MESSAGE_NO_SESSION, "T10", 2);
        assertCommandFailure(new MarkAllCommand(new Week(2), Status.PRESENT), model, expectedMessage);
    }

    @Test
    public void execute_noStudentsDisplayed_failure() {
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_NO_PERSONS);
        assertCommandFailure(new MarkAllCommand(new Week(1), Status.PRESENT), model,
                MarkAllCommand.MESSAGE_NO_STUDENTS);
    }

    @Test
    public void execute_filteredList_keepsDisplayedList() throws Exception {
        model.showGroup(T09);
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());

        new MarkAllCommand(new Week(1), Status.PRESENT).execute(model);

        assertEquals(displayedBefore.size(), model.getFilteredPersonList().size());
        for (int i = 0; i < displayedBefore.size(); i++) {
            assertTrue(displayedBefore.get(i).isSamePerson(model.getFilteredPersonList().get(i)));
        }
    }

    @Test
    public void equals() {
        MarkAllCommand standardCommand = new MarkAllCommand(new Week(1), Status.PRESENT);

        // same values -> returns true
        assertTrue(standardCommand.equals(new MarkAllCommand(new Week(1), Status.PRESENT)));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different week -> returns false
        assertFalse(standardCommand.equals(new MarkAllCommand(new Week(2), Status.PRESENT)));

        // different status -> returns false
        assertFalse(standardCommand.equals(new MarkAllCommand(new Week(1), Status.ABSENT)));
    }

    @Test
    public void toStringMethod() {
        MarkAllCommand markAllCommand = new MarkAllCommand(new Week(5), Status.ABSENT);
        String expected = MarkAllCommand.class.getCanonicalName() + "{week=5, status=ABSENT}";
        assertEquals(expected, markAllCommand.toString());
    }
}
