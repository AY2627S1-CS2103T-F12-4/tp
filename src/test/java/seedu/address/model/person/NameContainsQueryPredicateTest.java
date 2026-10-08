package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsQueryPredicateTest {

    @Test
    public void constructor_nullQuery_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NameContainsQueryPredicate(null));
    }

    @Test
    public void constructor_invalidQuery_throwsIllegalArgumentException() {
        for (String query : new String[]{"", " \t ", "123", "a".repeat(101), "John/Tan"}) {
            assertThrows(IllegalArgumentException.class, NameContainsQueryPredicate.MESSAGE_CONSTRAINTS, () ->
                    new NameContainsQueryPredicate(query));
        }
    }

    @Test
    public void isValidQuery_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> NameContainsQueryPredicate.isValidQuery(null));
    }

    @Test
    public void isValidQuery_supportedCharacters_returnsTrue() {
        for (String query : new String[]{"A", "Anne-Marie", "O'Neil", "O\u2019Neil", "J. Tan",
            "\u00c9lodie", "E\u0301lodie", "a".repeat(100), "\ud801\udc00".repeat(100)}) {
            assertTrue(NameContainsQueryPredicate.isValidQuery(query), query);
        }
    }

    @Test
    public void isValidQuery_unsupportedCharactersOrLength_returnsFalse() {
        for (String query : new String[]{"", " ", "123", "John2", ".'-", "\u0301", "John@Tan",
            "John/Tan", "John_Tan", "John\u0000Tan", "a".repeat(101), "\ud801\udc00".repeat(101)}) {
            assertFalse(NameContainsQueryPredicate.isValidQuery(query), query);
        }
    }

    @Test
    public void test_caseInsensitiveSubstring_returnsTrue() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("jOhN");
        assertTrue(predicate.test(person("John Tan")));
        assertTrue(predicate.test(person("Johnny Lim")));
        assertTrue(predicate.test(person("Johnson Ng")));
        assertTrue(new NameContainsQueryPredicate("ohn").test(person("John Tan")));
    }

    @Test
    public void test_phraseRequiresContiguousWordsInOrder() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("john tan");
        assertTrue(predicate.test(person("John Tan")));
        assertTrue(predicate.test(person("John Tanner")));
        assertTrue(predicate.test(person("Alex John Tan")));
        assertFalse(predicate.test(person("John Lim")));
        assertFalse(predicate.test(person("Mei Tan")));
        assertFalse(predicate.test(person("Tan John")));
        assertFalse(predicate.test(person("John Mei Tan")));
    }

    @Test
    public void test_queryAndNameWhitespace_normalized() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate(" \t John \n Tan \u00a0");
        assertEquals("John Tan", predicate.getQuery());
        assertTrue(predicate.test(person("John   Tan  ")));
        assertTrue(predicate.isWholeWordMatch(person("John   Tan  ")));
    }

    @Test
    public void test_accentsAndPunctuation_remainSignificant() {
        Person elodie = person("Elodie");
        assertFalse(new NameContainsQueryPredicate("\u00c9lodie").test(elodie));
        assertFalse(new NameContainsQueryPredicate("E\u0301lodie").test(elodie));
        assertFalse(new NameContainsQueryPredicate("O'Neil").test(person("ONeil")));
        assertFalse(new NameContainsQueryPredicate("Anne-Marie").test(person("Anne Marie")));
    }

    @Test
    public void test_nonNameFields_doNotMatch() {
        Person student = new PersonBuilder().withName("Alice").withEmail("john@example.com")
                .withAddress("John Street").withTags("John").build();
        assertFalse(new NameContainsQueryPredicate("John").test(student));
    }

    @Test
    public void test_defaultLocale_doesNotChangeMatching() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("LIM");
            assertTrue(predicate.test(person("John Lim")));
            assertTrue(predicate.isWholeWordMatch(person("John Lim")));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void isWholeWordMatch_singleWord_respectsWordBoundaries() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("john");
        assertTrue(predicate.isWholeWordMatch(person("John")));
        assertTrue(predicate.isWholeWordMatch(person("John Tan")));
        assertTrue(predicate.isWholeWordMatch(person("Alex John")));
        assertTrue(predicate.isWholeWordMatch(person("Alex John Tan")));
        assertFalse(predicate.isWholeWordMatch(person("Johnny Lim")));
        assertFalse(predicate.isWholeWordMatch(person("Ajohn Tan")));
        assertFalse(predicate.isWholeWordMatch(person("Alice")));
    }

    @Test
    public void isWholeWordMatch_phrase_respectsBothBoundaries() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("john tan");
        assertTrue(predicate.isWholeWordMatch(person("John Tan")));
        assertTrue(predicate.isWholeWordMatch(person("Alex John Tan Lim")));
        assertFalse(predicate.isWholeWordMatch(person("John Tanner")));
        assertFalse(predicate.isWholeWordMatch(person("Ajohn Tan")));
        assertFalse(predicate.isWholeWordMatch(person("Tan John")));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("John");
        assertThrows(NullPointerException.class, () -> predicate.test(null));
        assertThrows(NullPointerException.class, () -> predicate.isWholeWordMatch(null));
    }

    @Test
    public void equals() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("John Tan");
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new NameContainsQueryPredicate(" John   Tan ")));
        assertFalse(predicate.equals(new NameContainsQueryPredicate("Tan John")));
        assertFalse(predicate.equals("John Tan"));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void toStringMethod() {
        NameContainsQueryPredicate predicate = new NameContainsQueryPredicate("John Tan");
        String expected = NameContainsQueryPredicate.class.getCanonicalName() + "{query=John Tan}";
        assertEquals(expected, predicate.toString());
    }

    private Person person(String name) {
        return new PersonBuilder().withName(name).build();
    }
}
