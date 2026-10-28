package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Scores how closely a search query resembles a student's name, so that a mistyped query such as
 * {@code sarh} can still reach {@code Sarah}.
 * <p>
 * Scores run from 0.0 (nothing in common) to 1.0 (identical). The measure is Jaro-Winkler, which
 * rewards a shared leading prefix. An edit distance normalized by length cannot do this job: at
 * short query lengths a single typo costs too much. {@code sarh} against {@code Sarah} is one edit
 * in five characters, scoring 0.80, while Jaro-Winkler scores that pair 0.95 and {@code sarh}
 * against {@code Sandra} 0.80, separating them cleanly.
 */
public final class NameSimilarityScorer {

    /**
     * Scores at or above this are close enough to show when nothing matches exactly.
     * <p>
     * Set where real typos fall rather than at a round number. Swapping two letters away from the
     * start of a name, as in {@code srah} for {@code Sarah}, scores 0.865, while a genuinely
     * different student such as {@code ben} against {@code Bernice} scores 0.848. The cut sits
     * between them. A lower bar would start suggesting on very little: {@code amy} against
     * {@code Amirah} is only 0.733.
     * <p>
     * The cost of being slightly generous is small, because a suggestion is only ever offered
     * once an exact search has already found nobody.
     */
    public static final double DEFAULT_THRESHOLD = 0.85;

    /** Weight given to each character of shared prefix, as defined by Winkler. */
    private static final double PREFIX_WEIGHT = 0.1;

    /** At most this many leading characters count towards the prefix bonus. */
    private static final int MAX_PREFIX_LENGTH = 4;

    private NameSimilarityScorer() {
        // utility class, not meant to be instantiated
    }

    /**
     * Returns how closely {@code query} resembles {@code name}, between 0.0 and 1.0.
     * <p>
     * Both sides are lowercased and split into words. Each query word is scored against its
     * best-matching name word and the result is the mean of those scores, so that
     * {@code jonh tan} reaches {@code John Tan} while a single-word query still scores against
     * whichever part of a full name it resembles.
     */
    public static double score(String query, String name) {
        requireNonNull(query);
        requireNonNull(name);

        List<String> queryWords = words(query);
        List<String> nameWords = words(name);
        if (queryWords.isEmpty() || nameWords.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (String queryWord : queryWords) {
            double best = 0.0;
            for (String nameWord : nameWords) {
                best = Math.max(best, jaroWinkler(queryWord, nameWord));
            }
            total += best;
        }
        double score = total / queryWords.size();
        assert score >= 0.0 && score <= 1.0 : "A name similarity score should be between 0.0 and 1.0";
        return score;
    }

    /**
     * Returns true if {@code query} resembles {@code name} closely enough to be worth showing
     * when an exact search has found nothing.
     */
    public static boolean isSimilar(String query, String name) {
        return score(query, name) >= DEFAULT_THRESHOLD;
    }

    /**
     * Returns the Jaro-Winkler similarity of two words, between 0.0 and 1.0.
     * <p>
     * Package-private so that the per-word measure can be tested on its own: a regression here
     * should point at the measure rather than at the averaging {@link #score} does over words.
     */
    static double jaroWinkler(String first, String second) {
        double jaro = jaro(first, second);
        assert jaro >= 0.0 && jaro <= 1.0 : "A Jaro similarity should be between 0.0 and 1.0";
        if (jaro == 0.0) {
            return 0.0;
        }
        return jaro + commonPrefixLength(first, second) * PREFIX_WEIGHT * (1.0 - jaro);
    }

    /**
     * Returns the Jaro similarity of two words: the mean of how much of each word is matched and
     * how much of that match is in the same order.
     */
    private static double jaro(String first, String second) {
        if (first.equals(second)) {
            return 1.0;
        }
        if (first.isEmpty() || second.isEmpty()) {
            return 0.0;
        }

        // Two characters match only if they are equal and lie within this many positions of each
        // other, which is what lets a character still count once a typo has shifted it.
        int window = Math.max(Math.max(first.length(), second.length()) / 2 - 1, 0);
        boolean[] firstMatched = new boolean[first.length()];
        boolean[] secondMatched = new boolean[second.length()];

        int matches = 0;
        for (int i = 0; i < first.length(); i++) {
            int from = Math.max(0, i - window);
            int to = Math.min(second.length(), i + window + 1);
            for (int j = from; j < to; j++) {
                if (secondMatched[j] || first.charAt(i) != second.charAt(j)) {
                    continue;
                }
                firstMatched[i] = true;
                secondMatched[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) {
            return 0.0;
        }

        // A transposition is a matched pair that appears in a different order in the two words,
        // which is what a finger swap such as "jonh" for "john" produces.
        int transpositions = 0;
        int secondIndex = 0;
        for (int i = 0; i < first.length(); i++) {
            if (!firstMatched[i]) {
                continue;
            }
            while (!secondMatched[secondIndex]) {
                secondIndex++;
            }
            if (first.charAt(i) != second.charAt(secondIndex)) {
                transpositions++;
            }
            secondIndex++;
        }

        double matchCount = matches;
        return (matchCount / first.length()
                + matchCount / second.length()
                + (matchCount - transpositions / 2.0) / matchCount) / 3.0;
    }

    /** Returns the length of the shared leading prefix, capped at {@link #MAX_PREFIX_LENGTH}. */
    private static int commonPrefixLength(String first, String second) {
        int limit = Math.min(Math.min(first.length(), second.length()), MAX_PREFIX_LENGTH);
        int length = 0;
        while (length < limit && first.charAt(length) == second.charAt(length)) {
            length++;
        }
        return length;
    }

    /** Lowercases {@code value} and splits it into words, discarding empty ones. */
    private static List<String> words(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).strip().split("(?U)\\s+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }
}
