package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TopSimilarStudentsTest {

    @Test
    public void consider_nameBelowTheThreshold_isNotKept() {
        TopSimilarStudents similar = new TopSimilarStudents("sarah");

        // Nothing about "Benjamin Lee" resembles "sarah", so suggesting them would be noise
        similar.consider(student("Benjamin Lee", "A0000001X"));

        assertTrue(similar.isEmpty());
        assertEquals(List.of(), similar.best());
    }

    @Test
    public void consider_mistypedName_isKept() {
        TopSimilarStudents similar = new TopSimilarStudents("sarh");
        Person sarah = student("Sarah Tan", "A0000001X");

        similar.consider(sarah);

        assertFalse(similar.isEmpty());
        assertEquals(List.of(sarah), similar.best());
    }

    @Test
    public void best_moreCandidatesThanTheCap_keepsOnlyTheClosest() {
        Person exact = student("Sarah", "A0000001X");
        Person close = student("Sarah Tan", "A0000002X");
        Person nearby = student("Sarahh", "A0000003X");
        Person further = student("Sarha", "A0000004X");

        TopSimilarStudents similar = new TopSimilarStudents("sarah");
        for (Person person : List.of(further, exact, nearby, close)) {
            similar.consider(person);
        }

        List<Person> best = similar.best();
        assertEquals(TopSimilarStudents.MAX_SUGGESTIONS, best.size());
        // The closest match leads, and the weakest of the four has been dropped
        assertEquals(exact, best.get(0));
        assertFalse(best.contains(further));
    }

    @Test
    public void best_equallyCloseNames_areOrderedByMatric() {
        Person later = student("Sarah Tan", "A0000002X");
        Person earlier = student("Sarah Tan", "A0000001X");

        TopSimilarStudents similar = new TopSimilarStudents("sarah tan");
        similar.consider(later);
        similar.consider(earlier);

        // Both score identically, so the order must not depend on which was offered first
        assertEquals(List.of(earlier, later), similar.best());
    }

    @Test
    public void best_noCandidatesOffered_isEmpty() {
        TopSimilarStudents similar = new TopSimilarStudents("sarah");

        assertTrue(similar.isEmpty());
        assertEquals(List.of(), similar.best());
    }

    @Test
    public void constructorAndConsider_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TopSimilarStudents(null));
        assertThrows(NullPointerException.class, () -> new TopSimilarStudents("sarah").consider(null));
    }

    private static Person student(String name, String matric) {
        return new PersonBuilder().withName(name).withMatric(matric).build();
    }
}
