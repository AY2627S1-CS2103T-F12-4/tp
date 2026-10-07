package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class GroupTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Group(null));
    }

    @Test
    public void constructor_invalidGroup_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Group("T1"));
    }

    @Test
    public void isValidGroup() {
        // null group
        assertThrows(NullPointerException.class, () -> Group.isValidGroup(null));

        // invalid groups
        assertFalse(Group.isValidGroup("")); // empty string
        assertFalse(Group.isValidGroup(" ")); // spaces only
        assertFalse(Group.isValidGroup("T1")); // only one digit
        assertFalse(Group.isValidGroup("T001")); // three digits
        assertFalse(Group.isValidGroup("09")); // no letter
        assertFalse(Group.isValidGroup("TGA01")); // three letters
        assertFalse(Group.isValidGroup("t09")); // lowercase letter
        assertFalse(Group.isValidGroup("T-09")); // hyphen
        assertFalse(Group.isValidGroup("09T")); // digits before letters
        assertFalse(Group.isValidGroup("T 09")); // space inside

        // valid groups
        assertTrue(Group.isValidGroup("T09"));
        assertTrue(Group.isValidGroup("TG01"));
        assertTrue(Group.isValidGroup("W00"));
    }

    @Test
    public void equals() {
        Group group = new Group("T09");

        // same values -> returns true
        assertTrue(group.equals(new Group("T09")));

        // same object -> returns true
        assertTrue(group.equals(group));

        // null -> returns false
        assertFalse(group.equals(null));

        // different types -> returns false
        assertFalse(group.equals(5.0f));

        // different values -> returns false
        assertFalse(group.equals(new Group("T10")));
    }
}
