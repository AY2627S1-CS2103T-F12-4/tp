package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CommentTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Comment(null));
    }

    @Test
    public void constructor_invalidComment_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Comment(""));
        assertThrows(IllegalArgumentException.class, () -> new Comment("   "));
        assertThrows(IllegalArgumentException.class, () -> new Comment("a".repeat(Comment.MAX_LENGTH + 1)));
    }

    @Test
    public void constructor_normalizesWhitespace() {
        assertEquals("attentive in tutorials", new Comment("  attentive   in \t tutorials  ").value);
    }

    @Test
    public void isValidComment() {
        // null -> returns false rather than throwing
        assertFalse(Comment.isValidComment(null));

        // blank
        assertFalse(Comment.isValidComment(""));
        assertFalse(Comment.isValidComment("   "));

        // over the limit, before and after normalizing
        assertFalse(Comment.isValidComment("a".repeat(Comment.MAX_LENGTH + 1)));

        // control characters
        assertFalse(Comment.isValidComment("line one\u0000line two"));

        // ordinary notes, including punctuation and digits that Name would reject
        assertTrue(Comment.isValidComment("attentive, asks good questions"));
        assertTrue(Comment.isValidComment("missed week 5 - follow up."));
        assertTrue(Comment.isValidComment("needs extra time"));

        // at the limit
        assertTrue(Comment.isValidComment("a".repeat(Comment.MAX_LENGTH)));
        // #154: a running history of short notes needs more room than the original 200 characters
        assertTrue(Comment.isValidComment("a".repeat(300)));

        // whitespace that collapses back under the limit is accepted
        assertTrue(Comment.isValidComment("  attentive  "));
    }

    @Test
    public void isEmpty() {
        assertTrue(Comment.EMPTY.isEmpty());
        assertFalse(new Comment("attentive").isEmpty());
    }

    @Test
    public void append_toEmpty_returnsAdditionWithoutSeparator() {
        assertEquals(new Comment("attentive"), Comment.EMPTY.append(new Comment("attentive")));
    }

    @Test
    public void append_emptyAddition_returnsThis() {
        Comment existing = new Comment("attentive");
        assertEquals(existing, existing.append(Comment.EMPTY));
    }

    @Test
    public void append_toExisting_joinsWithSeparator() {
        Comment existing = new Comment("attentive");
        assertEquals(new Comment("attentive; asks good questions"),
                existing.append(new Comment("asks good questions")));
    }

    @Test
    public void append_resultOverLimit_throwsIllegalArgumentException() {
        Comment existing = new Comment("a".repeat(Comment.MAX_LENGTH - 1));
        assertThrows(IllegalArgumentException.class, () -> existing.append(new Comment("bb")));
    }

    @Test
    public void append_resultExactlyAtLimit_succeeds() {
        // existing + "; " + addition == MAX_LENGTH
        Comment existing = new Comment("a".repeat(Comment.MAX_LENGTH - 3));
        Comment combined = existing.append(new Comment("b"));
        assertEquals(Comment.MAX_LENGTH, combined.value.length());
    }

    @Test
    public void lengthAfterAppending() {
        Comment existing = new Comment("abc");
        assertEquals(3 + Comment.SEPARATOR.length() + 2, existing.lengthAfterAppending(new Comment("de")));
        assertEquals(3, existing.lengthAfterAppending(Comment.EMPTY));
        assertEquals(2, Comment.EMPTY.lengthAfterAppending(new Comment("de")));
    }

    @Test
    public void equals() {
        Comment comment = new Comment("attentive");

        // same values -> returns true
        assertTrue(comment.equals(new Comment("attentive")));

        // same object -> returns true
        assertTrue(comment.equals(comment));

        // null -> returns false
        assertFalse(comment.equals(null));

        // different types -> returns false
        assertFalse(comment.equals(5.0f));

        // different values -> returns false
        assertFalse(comment.equals(new Comment("distracted")));
    }

    @Test
    public void hashCode_sameValue_isConsistent() {
        assertEquals(new Comment("attentive").hashCode(), new Comment("attentive").hashCode());
    }

    @Test
    public void toStringMethod() {
        assertEquals("attentive", new Comment("attentive").toString());
        assertEquals("", Comment.EMPTY.toString());
    }
}
