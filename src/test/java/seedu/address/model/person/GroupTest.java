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
        assertThrows(IllegalArgumentException.class, () -> new Group("T9"));
        assertThrows(IllegalArgumentException.class, () -> new Group("t09"));
    }

    @Test
    public void isValidGroup() {
        assertFalse(Group.isValidGroup(null));
        assertFalse(Group.isValidGroup(""));
        assertFalse(Group.isValidGroup("T9"));
        assertFalse(Group.isValidGroup("T009"));
        assertFalse(Group.isValidGroup("T-09"));
        assertFalse(Group.isValidGroup("t09"));
        assertTrue(Group.isValidGroup("T09"));
        assertTrue(Group.isValidGroup("TG01"));
    }
}
