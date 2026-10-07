package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Group;
import seedu.address.model.session.exceptions.DuplicateSessionException;

/**
 * A list of sessions that enforces uniqueness between its elements and does not allow nulls.
 * A session is considered unique by comparing using {@code Session#isSameSession(Session)}, so a tutorial group
 * has at most one session per teaching week.
 *
 * Supports a minimal set of list operations.
 *
 * @see Session#isSameSession(Session)
 */
public class UniqueSessionList implements Iterable<Session> {

    private final ObservableList<Session> internalList = FXCollections.observableArrayList();
    private final ObservableList<Session> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains a session with the same group and week as the given argument.
     */
    public boolean contains(Session toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameSession);
    }

    /**
     * Adds a session to the list.
     * The group must not already have a session in the same week.
     */
    public void add(Session toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateSessionException();
        }
        internalList.add(toAdd);
    }

    /**
     * Returns the session of {@code group} in {@code week}, if there is one.
     */
    public Optional<Session> find(Group group, Week week) {
        requireAllNonNull(group, week);
        return internalList.stream().filter(session -> session.isFor(group, week)).findFirst();
    }

    /**
     * Replaces the contents of this list with {@code sessions}.
     * {@code sessions} must not contain duplicate sessions.
     */
    public void setSessions(List<Session> sessions) {
        requireAllNonNull(sessions);
        if (!sessionsAreUnique(sessions)) {
            throw new DuplicateSessionException();
        }

        internalList.setAll(sessions);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Session> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Session> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniqueSessionList otherUniqueSessionList)) {
            return false;
        }

        return internalList.equals(otherUniqueSessionList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code sessions} contains only unique sessions.
     */
    private boolean sessionsAreUnique(List<Session> sessions) {
        for (int i = 0; i < sessions.size() - 1; i++) {
            for (int j = i + 1; j < sessions.size(); j++) {
                if (sessions.get(i).isSameSession(sessions.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
