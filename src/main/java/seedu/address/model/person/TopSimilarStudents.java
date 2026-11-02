package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Collects the students whose names are closest to a search query, keeping only the best few.
 * <p>
 * Students are offered one at a time with {@link #consider(Person)}, and only once a search has
 * already come back empty, so a search that finds someone never pays for scoring names.
 * <p>
 * Only names scoring at least {@link NameSimilarityScorer#DEFAULT_THRESHOLD} are kept, so a query
 * that resembles nobody produces nothing rather than the least bad student in the roster.
 */
public class TopSimilarStudents {

    /** Most students to suggest. Beyond a few, a list of guesses is no longer a hint. */
    public static final int MAX_SUGGESTIONS = 3;

    private final String query;

    /**
     * The kept students, ordered worst first. A heap bounded at {@link #MAX_SUGGESTIONS} keeps the
     * best few in one pass, where sorting every candidate would do far more work for the same
     * three names.
     */
    private final PriorityQueue<ScoredStudent> kept = new PriorityQueue<>();

    /**
     * Creates a collector for {@code query}, the name the user searched for.
     */
    public TopSimilarStudents(String query) {
        this.query = requireNonNull(query);
    }

    /**
     * Offers {@code person} as a near miss, keeping them only if they score well enough and rank
     * among the best seen so far.
     */
    public void consider(Person person) {
        requireNonNull(person);
        double score = NameSimilarityScorer.score(query, person.getName().fullName);
        if (score < NameSimilarityScorer.DEFAULT_THRESHOLD) {
            return;
        }
        kept.add(new ScoredStudent(person, score));
        if (kept.size() > MAX_SUGGESTIONS) {
            // The head is the worst of the kept students, so dropping it leaves the best behind
            kept.poll();
        }
    }

    /** Returns whether any student resembled the query closely enough to suggest. */
    public boolean isEmpty() {
        return kept.isEmpty();
    }

    /**
     * Returns the kept students, closest match first.
     */
    public List<Person> best() {
        List<ScoredStudent> ordered = new ArrayList<>(kept);
        ordered.sort(Comparator.reverseOrder());
        return ordered.stream().map(ScoredStudent::person).toList();
    }

    /**
     * A student and how closely their name matched, ordered worst first.
     * Equally close students are ordered by matriculation number so that a tie is broken the same
     * way every time, rather than by whichever the roster happened to offer first.
     */
    private record ScoredStudent(Person person, double score) implements Comparable<ScoredStudent> {
        @Override
        public int compareTo(ScoredStudent other) {
            int byScore = Double.compare(score, other.score);
            return byScore != 0
                    ? byScore
                    : other.person.getMatric().value.compareTo(person.getMatric().value);
        }
    }
}
