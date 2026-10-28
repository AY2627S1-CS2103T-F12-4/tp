package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Matches students against every supplied name, matriculation-number and tutorial-group filter.
 */
public class StudentMatchesFiltersPredicate implements Predicate<Person> {
    private final NameContainsQueryPredicate namePredicate;
    private final Matric matric;
    private final Group group;

    /**
     * Creates a combined filter. A null field means that filter was not supplied.
     * At least one filter must be supplied.
     */
    public StudentMatchesFiltersPredicate(NameContainsQueryPredicate namePredicate, Matric matric, Group group) {
        checkArgument(namePredicate != null || matric != null || group != null, "Provide at least one search filter.");
        this.namePredicate = namePredicate;
        this.matric = matric;
        this.group = group;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return (namePredicate == null || namePredicate.test(person)) && matchesExceptName(person);
    }

    /**
     * Returns whether every filter other than the name matches {@code person}.
     * A student whose name was mistyped should still be suggested only if they belong to the
     * group and matriculation number that were searched for, so those filters are applied on
     * their own when looking for near misses.
     */
    public boolean matchesExceptName(Person person) {
        requireNonNull(person);
        return (matric == null || matric.equals(person.getMatric()))
                && (group == null || group.equals(person.getGroup()));
    }

    public Optional<Group> getGroup() {
        return Optional.ofNullable(group);
    }

    public Optional<NameContainsQueryPredicate> getNamePredicate() {
        return Optional.ofNullable(namePredicate);
    }

    public boolean isNameOnly() {
        return namePredicate != null && matric == null && group == null;
    }

    /**
     * Describes the normalized filters in a consistent order for command feedback.
     */
    public String describeFilters() {
        List<String> filters = new ArrayList<>();
        if (namePredicate != null) {
            filters.add("n/\"" + namePredicate.getQuery() + "\"");
        }
        if (matric != null) {
            filters.add("m/" + matric.value);
        }
        if (group != null) {
            filters.add("grp/" + group.value);
        }
        return String.join(" ", filters);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentMatchesFiltersPredicate otherPredicate
                && Objects.equals(namePredicate, otherPredicate.namePredicate)
                && Objects.equals(matric, otherPredicate.matric)
                && Objects.equals(group, otherPredicate.group);
    }

    @Override
    public String toString() {
        return describeFilters();
    }
}
