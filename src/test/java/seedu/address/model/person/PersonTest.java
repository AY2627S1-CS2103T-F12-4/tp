package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GROUP_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_MATRIC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different matriculation number -> returns false
        editedAlice = new PersonBuilder(ALICE).withMatric(VALID_MATRIC_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different group -> returns false
        editedAlice = new PersonBuilder(ALICE).withGroup(VALID_GROUP_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void constructor_attendanceForOtherGroup_throwsIllegalArgumentException() {
        // ALICE is in T09
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_INVALID_ATTENDANCES, () ->
                new PersonBuilder(ALICE).withAttendance(T10_WEEK_1, Status.PRESENT).build());
    }

    @Test
    public void getAttendance() {
        Person alicePresent = new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build();

        // marked session -> returns the attendance
        assertEquals(Optional.of(new Attendance(T09_WEEK_1, Status.PRESENT)),
                alicePresent.getAttendance(T09_WEEK_1));

        // unmarked session -> returns empty
        assertEquals(Optional.empty(), alicePresent.getAttendance(T09_WEEK_2));
    }

    @Test
    public void withAttendance() {
        // first mark -> attendance added, original unchanged
        Person alicePresent = ALICE.withAttendance(new Attendance(T09_WEEK_1, Status.PRESENT));
        assertEquals(new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build(), alicePresent);
        assertTrue(ALICE.getAttendances().isEmpty());

        // second mark for the same session -> attendance replaced, not added
        Person aliceAbsent = alicePresent.withAttendance(new Attendance(T09_WEEK_1, Status.ABSENT));
        assertEquals(new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.ABSENT).build(), aliceAbsent);

        // mark for another session -> both kept
        Person aliceTwoWeeks = aliceAbsent.withAttendance(new Attendance(T09_WEEK_2, Status.PRESENT));
        assertEquals(2, aliceTwoWeeks.getAttendances().size());
    }

    @Test
    public void equals_differentAttendance_returnsFalse() {
        Person alicePresent = new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        assertFalse(ALICE.equals(alicePresent));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", matric=" + ALICE.getMatric()
                + ", group=" + ALICE.getGroup() + ", tags=" + ALICE.getTags()
                + ", attendances=" + ALICE.getAttendances() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
