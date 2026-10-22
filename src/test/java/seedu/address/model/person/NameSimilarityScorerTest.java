package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.NameSimilarityScorer.DEFAULT_THRESHOLD;
import static seedu.address.model.person.NameSimilarityScorer.isSimilar;
import static seedu.address.model.person.NameSimilarityScorer.jaroWinkler;
import static seedu.address.model.person.NameSimilarityScorer.score;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameSimilarityScorerTest {

    private static final double TOLERANCE = 0.001;

    @Test
    public void score_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> score(null, "Sarah"));
        assertThrows(NullPointerException.class, () -> score("sarah", null));
    }

    // The case the feature exists for: a typo reaches the student, a different name does not.
    @Test
    public void isSimilar_mistypedQuery_reachesTheIntendedStudentOnly() {
        assertTrue(isSimilar("sarh", "Sarah"));
        assertFalse(isSimilar("sarh", "Sandra"));
    }

    @Test
    public void score_mistypedQuery_ranksTheIntendedStudentHigher() {
        assertTrue(score("sarh", "Sarah") > score("sarh", "Sandra"));
    }

    @Test
    public void score_identicalStrings_isOne() {
        assertEquals(1.0, score("Sarah", "Sarah"), TOLERANCE);
        assertEquals(1.0, score("Wei Ming Tan", "Wei Ming Tan"), TOLERANCE);
    }

    @Test
    public void score_ignoresCaseAndSurroundingWhitespace() {
        assertEquals(1.0, score("  sArAh  ", "SARAH"), TOLERANCE);
        assertEquals(score("john tan", "John Tan"), score("JOHN   TAN", "john tan"), TOLERANCE);
    }

    @Test
    public void score_nothingInCommon_isZero() {
        assertEquals(0.0, score("xyz", "Sarah"), TOLERANCE);
        assertFalse(isSimilar("xyz", "Sarah"));
    }

    @Test
    public void score_emptyInput_isZeroRatherThanThrowing() {
        assertEquals(0.0, score("", "Sarah"), TOLERANCE);
        assertEquals(0.0, score("sarah", ""), TOLERANCE);
        assertEquals(0.0, score("   ", "Sarah"), TOLERANCE);
    }

    @Test
    public void score_transposedCharacters_staysAboveThreshold() {
        // A finger swap is the typo a prefix comparison alone would miss.
        assertTrue(isSimilar("jonh", "John"));
        assertTrue(isSimilar("joh ntan", "John Tan"));
    }

    @Test
    public void score_transpositionAwayFromThePrefix_fallsJustBelowTheThreshold() {
        // "sahra" for "Sarah" scores 0.893, which the 0.9 threshold rejects by 0.007. The swap
        // sits past the shared prefix, so it earns a smaller Winkler bonus than "jonh" for
        // "John" does. This test pins the boundary: if the threshold is ever lowered, that
        // should be a decision someone makes here rather than a surprise.
        assertFalse(isSimilar("sahra", "Sarah"));
        assertEquals(0.893, jaroWinkler("sahra", "sarah"), TOLERANCE);
    }

    @Test
    public void score_droppedCharacter_staysAboveThreshold() {
        assertTrue(isSimilar("sarh", "Sarah"));
        assertTrue(isSimilar("bernic", "Bernice"));
    }

    @Test
    public void score_singleWordQuery_matchesAnyWordOfAFullName() {
        assertTrue(isSimilar("sarh", "Sarah Tan"));
        assertTrue(isSimilar("tan", "Wei Ming Tan"));
    }

    @Test
    public void score_multiWordQuery_isCarriedByItsBestMatchingWords() {
        assertTrue(isSimilar("jonh tan", "John Tan"));

        // Both words wrong: correctly rejected rather than rescued by averaging.
        assertFalse(isSimilar("alex lim", "John Tan"));
    }

    @Test
    public void score_differentStudentInTheSameGroup_isRejected() {
        // Names that share a first letter should not be dragged in by the prefix bonus alone.
        assertFalse(isSimilar("alex", "Alicia"));
        assertFalse(isSimilar("ben", "Bernice"));
    }

    @Test
    public void score_isBoundedBetweenZeroAndOne() {
        String[] queries = {"sarh", "jonh tan", "xyz", "a", "Wei Ming Tan"};
        String[] names = {"Sarah", "Sandra", "John Tan", "Wei Ming Tan", "Alicia Lim"};
        for (String query : queries) {
            for (String name : names) {
                double result = score(query, name);
                assertTrue(result >= 0.0 && result <= 1.0,
                        "score(" + query + ", " + name + ") was " + result);
            }
        }
    }

    @Test
    public void isSimilar_agreesWithTheThreshold() {
        assertEquals(score("sarh", "Sarah") >= DEFAULT_THRESHOLD, isSimilar("sarh", "Sarah"));
        assertEquals(score("sarh", "Sandra") >= DEFAULT_THRESHOLD, isSimilar("sarh", "Sandra"));
    }

    // The per-word measure, tested directly so that a regression points here rather than at the
    // averaging that score() does across words.
    @Test
    public void jaroWinkler_publishedPairs() {
        assertEquals(0.953, jaroWinkler("sarh", "sarah"), TOLERANCE);
        assertEquals(0.800, jaroWinkler("sarh", "sandra"), TOLERANCE);
        assertEquals(0.933, jaroWinkler("jonh", "john"), TOLERANCE);
    }

    @Test
    public void jaroWinkler_identicalAndDisjointWords() {
        assertEquals(1.0, jaroWinkler("sarah", "sarah"), TOLERANCE);
        assertEquals(0.0, jaroWinkler("abc", "xyz"), TOLERANCE);
        assertEquals(0.0, jaroWinkler("", "sarah"), TOLERANCE);
    }

    @Test
    public void jaroWinkler_prefixBonusRewardsASharedStart() {
        // Same characters, same count: the pair sharing a longer prefix scores higher.
        assertTrue(jaroWinkler("sarh", "sarah") > jaroWinkler("asrh", "sarah"));
    }
}
