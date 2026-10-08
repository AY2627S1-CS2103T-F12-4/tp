package seedu.address.model.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StatusTest {

    @Test
    public void isValidStatus() {
        // null status
        assertThrows(NullPointerException.class, () -> Status.isValidStatus(null));

        // invalid statuses
        assertFalse(Status.isValidStatus("")); // empty string
        assertFalse(Status.isValidStatus("late")); // not a status
        assertFalse(Status.isValidStatus("unmarked")); // unmarked is not a stored status
        assertFalse(Status.isValidStatus("p")); // abbreviation
        assertFalse(Status.isValidStatus(" present")); // leading space

        // valid statuses, in any letter case
        assertTrue(Status.isValidStatus("present"));
        assertTrue(Status.isValidStatus("ABSENT"));
        assertTrue(Status.isValidStatus("Present"));
    }

    @Test
    public void fromString_validStatus_returnsStatus() {
        assertEquals(Status.PRESENT, Status.fromString("present"));
        assertEquals(Status.ABSENT, Status.fromString("Absent"));
    }

    @Test
    public void fromString_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.fromString("late"));
    }
}
