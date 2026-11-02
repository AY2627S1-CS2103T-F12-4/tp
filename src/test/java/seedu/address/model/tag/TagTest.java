package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void equals_comparesExactSpelling() {
        Tag tag = new Tag("exchange");

        assertTrue(tag.equals(new Tag("exchange")));
        assertEquals(tag.hashCode(), new Tag("exchange").hashCode());

        // A change of case is a real change, so an edit that only recases a tag is saved
        assertFalse(tag.equals(new Tag("Exchange")));
        assertFalse(tag.equals(new Tag("exchanges")));
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("exchange"));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName("needs help")); // spaces
        assertFalse(Tag.isValidTagName("#friend")); // other symbols
        assertFalse(Tag.isValidTagName("-rep")); // leading hyphen
        assertFalse(Tag.isValidTagName("rep-")); // trailing hyphen
        assertFalse(Tag.isValidTagName("class--rep")); // consecutive hyphens
        assertFalse(Tag.isValidTagName("-")); // hyphen only

        // valid tag names
        assertTrue(Tag.isValidTagName("exchange")); // letters only
        assertTrue(Tag.isValidTagName("CS2103")); // letters and digits
        assertTrue(Tag.isValidTagName("class-rep")); // one hyphen
        assertTrue(Tag.isValidTagName("needs-follow-up")); // several hyphens
        assertTrue(Tag.isValidTagName("week-5")); // hyphen before a digit
    }

    @Test
    public void isValidTagName_longerThanMaxLength_returnsTrue() {
        // Only new input is limited, so saved tags of any length still load
        assertTrue(Tag.isValidTagName("a".repeat(Tag.MAX_LENGTH + 1)));
    }

    @Test
    public void isValidTagName_veryLongHyphenatedTag_returnsWithoutError() {
        // Thousands of hyphenated parts used to overflow the stack inside the regex
        assertTrue(Tag.isValidTagName("a" + "-a".repeat(5000)));
        assertFalse(Tag.isValidTagName("a" + "-a".repeat(5000) + "-")); // ends with a hyphen
    }

}
