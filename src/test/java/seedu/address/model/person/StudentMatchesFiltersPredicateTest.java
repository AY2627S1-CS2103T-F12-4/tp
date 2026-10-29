package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StudentMatchesFiltersPredicateTest {
    private final NameContainsQueryPredicate name = new NameContainsQueryPredicate("John");
    private final Matric matric = new Matric("A0000001X");
    private final Group group = new Group("T09");
    private final Person student = new PersonBuilder().withName("John Tan").withMatric(matric.value)
            .withGroup(group.value).build();

    @Test
    public void constructor_noFilters_rejectsAnUnrestrictedSearch() {
        assertThrows(IllegalArgumentException.class, () -> new StudentMatchesFiltersPredicate(null, null, null));
    }

    @Test
    public void test_everyFilterCombination_requiresEachSuppliedField() {
        Person differentName = new PersonBuilder(student).withName("Alice Tan").build();
        Person differentMatric = new PersonBuilder(student).withMatric("A0000002X").build();
        Person differentGroup = new PersonBuilder(student).withGroup("T10").build();
        for (Filters filters : filterCombinations()) {
            StudentMatchesFiltersPredicate predicate = new StudentMatchesFiltersPredicate(
                    filters.name(), filters.matric(), filters.group());
            assertTrue(predicate.test(student));
            assertEquals(filters.name() == null, predicate.test(differentName));
            assertEquals(filters.matric() == null, predicate.test(differentMatric));
            assertEquals(filters.group() == null, predicate.test(differentGroup));
        }
    }

    @Test
    public void test_nullStudent_rejectsInvalidInput() {
        assertThrows(NullPointerException.class, () -> new StudentMatchesFiltersPredicate(name, null, null).test(null));
    }

    @Test
    public void equals_comparesEveryFilterAndHandlesOtherTypes() {
        StudentMatchesFiltersPredicate predicate = new StudentMatchesFiltersPredicate(name, matric, group);
        StudentMatchesFiltersPredicate equal = new StudentMatchesFiltersPredicate(
                new NameContainsQueryPredicate("John"), new Matric("A0000001X"), new Group("T09"));
        assertEquals(predicate, predicate);
        assertEquals(predicate, equal);
        assertEquals(equal, predicate);
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("John"));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(
                new NameContainsQueryPredicate("Alice"), matric, group)));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(name, new Matric("A0000002X"), group)));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(name, matric, new Group("T10"))));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(null, matric, group)));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(name, null, group)));
        assertFalse(predicate.equals(new StudentMatchesFiltersPredicate(name, matric, null)));
    }

    @Test
    public void hashCode_equalFilterCombinations_supportHashSetLookup() {
        for (Filters filters : filterCombinations()) {
            StudentMatchesFiltersPredicate predicate = new StudentMatchesFiltersPredicate(
                    filters.name(), filters.matric(), filters.group());
            StudentMatchesFiltersPredicate equal = new StudentMatchesFiltersPredicate(
                    filters.name() == null ? null : new NameContainsQueryPredicate("John"),
                    filters.matric() == null ? null : new Matric(matric.value),
                    filters.group() == null ? null : new Group(group.value));
            Set<StudentMatchesFiltersPredicate> predicates = new HashSet<>();
            predicates.add(predicate);
            assertEquals(predicate.hashCode(), equal.hashCode());
            assertTrue(predicates.contains(equal));
        }
    }

    private List<Filters> filterCombinations() {
        return List.of(new Filters(name, null, null), new Filters(null, matric, null), new Filters(null, null, group),
                new Filters(name, matric, null), new Filters(name, null, group), new Filters(null, matric, group),
                new Filters(name, matric, group));
    }

    private record Filters(NameContainsQueryPredicate name, Matric matric, Group group) {
    }
}
