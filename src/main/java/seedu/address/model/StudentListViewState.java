package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.model.person.Group;
import seedu.address.model.person.Person;

/**
 * Captures the live student filter, ordering and group indicators for save-failure recovery.
 * A null predicate shows all students, and a null comparator uses the stored roster order.
 */
public record StudentListViewState(Predicate<? super Person> predicate, Comparator<? super Person> comparator,
        Optional<Group> activeGroup, boolean isShowingAllStudents) {

    public StudentListViewState {
        requireNonNull(activeGroup);
    }
}
