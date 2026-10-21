package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_whitespace_returnsNormalizedName() {
        assertEquals("Alex Tan", new Name(" \tAlex\u2003  Tan\n ").fullName);
        assertEquals("Alex Tan", new Name("Alex" + " ".repeat(80) + "Tan").fullName);
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // unsupported punctuation
        assertFalse(Name.isValidName("peter*"));
        assertFalse(Name.isValidName("12345"));
        assertFalse(Name.isValidName("peter the 2nd"));
        assertFalse(Name.isValidName("'Peter"));
        assertFalse(Name.isValidName("Peter-"));
        assertFalse(Name.isValidName(".Peter"));
        assertFalse(Name.isValidName("Ravi s/other Kumar"));
        assertFalse(Name.isValidName("Ravi s/oKumar"));
        assertFalse(Name.isValidName("John/Tan"));
        assertFalse(Name.isValidName("Ravi s/o Kumar/"));
        assertFalse(Name.isValidName("\u0301Elodie"));
        assertFalse(Name.isValidName("A".repeat(71)));
        assertFalse(Name.isValidName("\ud801\udc00".repeat(71)));

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("A")); // single-letter name
        assertTrue(Name.isValidName("O'Neil"));
        assertTrue(Name.isValidName("O\u2019Neil"));
        assertTrue(Name.isValidName("Anne-Marie"));
        assertTrue(Name.isValidName("J. Tan"));
        assertTrue(Name.isValidName("Tan J."));
        assertTrue(Name.isValidName("J."));
        assertTrue(Name.isValidName("Ravi s/o Kumar"));
        assertTrue(Name.isValidName("Rani D/O Kumar"));
        assertTrue(Name.isValidName("Arun a/l Ravi"));
        assertTrue(Name.isValidName("Meena a/p Kumar"));
        assertTrue(Name.isValidName("E\u0301lodie Tan"));
        assertTrue(Name.isValidName("\u00c9lodie Tan"));
        assertTrue(Name.isValidName("A".repeat(70)));
        assertTrue(Name.isValidName("\ud801\udc00".repeat(70)));
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Junior")); // long names
    }

    @Test
    public void constructor_nameAbbreviation_countsOriginalCharactersAndPreservesSpelling() {
        String atLimit = "A".repeat(64) + " s/o B";
        assertEquals(atLimit, new Name(atLimit).fullName);
        assertFalse(Name.isValidName("A" + atLimit));
        assertEquals("Ravi S/O Kumar", new Name("  Ravi   S/O Kumar  ").fullName);
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
