package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertThrows(IllegalArgumentException.class, () -> new Matric(""));
        assertThrows(IllegalArgumentException.class, () -> new Matric("B0123456X"));
        assertThrows(IllegalArgumentException.class, () -> new Matric("A012345X"));
    }

    @Test
    public void constructor_unnormalizedMatric_throwsIllegalArgumentException() {
        // Matric takes an already uppercased value; ParserUtil#parseMatric does the normalising.
        assertThrows(IllegalArgumentException.class, () -> new Matric("a0123456x"));
        assertThrows(IllegalArgumentException.class, () -> new Matric("A0123456x"));
        assertThrows(IllegalArgumentException.class, () -> new Matric(" A0123456X "));
    }

    @Test
    public void isValidMatric() {
        // null -> returns false rather than throwing
        assertFalse(Matric.isValidMatric(null));

        // blank
        assertFalse(Matric.isValidMatric(""));
        assertFalse(Matric.isValidMatric(" "));

        // wrong leading letter
        assertFalse(Matric.isValidMatric("B0123456X"));
        assertFalse(Matric.isValidMatric("00123456X"));

        // wrong number of digits
        assertFalse(Matric.isValidMatric("A012345X"));
        assertFalse(Matric.isValidMatric("A01234567X"));

        // missing or non-letter suffix
        assertFalse(Matric.isValidMatric("A0123456"));
        assertFalse(Matric.isValidMatric("A01234567"));

        // stray characters
        assertFalse(Matric.isValidMatric("A0123456X "));
        assertFalse(Matric.isValidMatric("A-123456X"));

        // lowercase is not valid: the value is expected to be normalised already
        assertFalse(Matric.isValidMatric("a0123456x"));
        assertFalse(Matric.isValidMatric("A0123456x"));

        // valid
        assertTrue(Matric.isValidMatric("A0123456X"));
        assertTrue(Matric.isValidMatric("A0000000A"));
        assertTrue(Matric.isValidMatric("A9999999Z"));
    }

    @Test
    public void equals() {
        Matric matric = new Matric("A0123456X");

        // same values -> returns true
        assertTrue(matric.equals(new Matric("A0123456X")));

        // same object -> returns true
        assertTrue(matric.equals(matric));

        // null -> returns false
        assertFalse(matric.equals(null));

        // different types -> returns false
        assertFalse(matric.equals(5.0f));

        // different values -> returns false
        assertFalse(matric.equals(new Matric("A0123456Y")));
    }

    @Test
    public void hashCode_sameValue_isConsistent() {
        assertEquals(new Matric("A0123456X").hashCode(), new Matric("A0123456X").hashCode());
    }

    @Test
    public void toStringMethod() {
        assertEquals("A0123456X", new Matric("A0123456X").toString());
    }
}
