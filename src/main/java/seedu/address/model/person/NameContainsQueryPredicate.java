package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's name contains a complete search phrase, ignoring case.
 */
public class NameContainsQueryPredicate implements Predicate<Person> {
    public static final String MESSAGE_CONSTRAINTS = "Invalid search keyword. Use 1-100 characters containing "
            + "letters, spaces, hyphens, apostrophes or full stops, with at least one letter.";

    private final String query;
    private final String normalizedQuery;

    /**
     * Creates a predicate from a valid search phrase, normalizing its whitespace.
     */
    public NameContainsQueryPredicate(String query) {
        requireNonNull(query);
        this.query = normalizeWhitespace(query);
        checkArgument(isValidQuery(this.query), MESSAGE_CONSTRAINTS);
        normalizedQuery = this.query.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether the query contains supported characters and at least one letter within the length limit.
     */
    public static boolean isValidQuery(String query) {
        requireNonNull(query);
        String normalized = normalizeWhitespace(query);
        return normalized.codePointCount(0, normalized.length()) <= 100
                && normalized.matches("[\\p{L}\\p{M} .'\u2019-]+")
                && normalized.matches(".*\\p{L}.*");
    }

    /**
     * Returns the search phrase with surrounding whitespace removed and internal whitespace collapsed.
     */
    public String getQuery() {
        return query;
    }

    @Override
    public boolean test(Person person) {
        return normalizeName(person).contains(normalizedQuery);
    }

    /**
     * Returns whether the complete query matches whole name words rather than part of a word.
     */
    public boolean isWholeWordMatch(Person person) {
        return (" " + normalizeName(person) + " ").contains(" " + normalizedQuery + " ");
    }

    private String normalizeName(Person person) {
        requireNonNull(person);
        return normalizeWhitespace(person.getName().fullName).toLowerCase(Locale.ROOT);
    }

    private static String normalizeWhitespace(String value) {
        return value.replaceAll("(?U)\\s+", " ").strip();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof NameContainsQueryPredicate otherPredicate && query.equals(otherPredicate.query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("query", query).toString();
    }
}
