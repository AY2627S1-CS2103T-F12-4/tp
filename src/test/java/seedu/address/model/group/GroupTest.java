package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }

    @Test
    public void constructor_validGroup_normalizesCode() {
        assertEquals("T09", new Group("  t09  ").code);
        assertEquals("TG01", new Group("tg01").code);
    }

    @Test
    public void isValidGroup() {
        assertFalse(Group.isValidGroup(null));
        assertFalse(Group.isValidGroup(""));
        assertFalse(Group.isValidGroup("T9"));
        assertFalse(Group.isValidGroup("T009"));
        assertFalse(Group.isValidGroup("T-09"));
        assertTrue(Group.isValidGroup("T09"));
        assertTrue(Group.isValidGroup("tg01"));
        assertTrue(Group.isValidGroup(" TG01 "));
    }
}
