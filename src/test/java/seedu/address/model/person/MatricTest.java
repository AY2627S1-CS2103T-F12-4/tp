package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class MatricTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Matric(null));
    }

    @Test
    public void constructor_invalidMatric_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Matric("A123"));
    }

    @Test
    public void isValidMatric() {
        // null matriculation number
        assertThrows(NullPointerException.class, () -> Matric.isValidMatric(null));

        // invalid matriculation numbers
        assertFalse(Matric.isValidMatric("")); // empty string
        assertFalse(Matric.isValidMatric(" ")); // spaces only
        assertFalse(Matric.isValidMatric("B0287654J")); // does not start with A
        assertFalse(Matric.isValidMatric("A028765J")); // only 6 digits
        assertFalse(Matric.isValidMatric("A02876543J")); // 8 digits
        assertFalse(Matric.isValidMatric("A0287654")); // no final letter
        assertFalse(Matric.isValidMatric("A0287654JK")); // two final letters
        assertFalse(Matric.isValidMatric("a0287654j")); // lowercase letters
        assertFalse(Matric.isValidMatric(" A0287654J")); // leading space
        assertFalse(Matric.isValidMatric("A0287654-")); // final character not a letter

        // valid matriculation numbers
        assertTrue(Matric.isValidMatric("A0287654J"));
        assertTrue(Matric.isValidMatric("A0000000A"));
        assertTrue(Matric.isValidMatric("A9999999Z"));
    }

    @Test
    public void equals() {
        Matric matric = new Matric("A0287654J");

        // same values -> returns true
        assertTrue(matric.equals(new Matric("A0287654J")));

        // same object -> returns true
        assertTrue(matric.equals(matric));

        // null -> returns false
        assertFalse(matric.equals(null));

        // different types -> returns false
        assertFalse(matric.equals(5.0f));

        // different values -> returns false
        assertFalse(matric.equals(new Matric("A0287654K")));
    }
}
