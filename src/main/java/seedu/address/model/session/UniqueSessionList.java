package seedu.address.model.session;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.group.Group;
import seedu.address.model.session.exceptions.DuplicateSessionException;

/**
 * A list of sessions that allows at most one session for each group and teaching week.
 */
public class UniqueSessionList implements Iterable<Session> {

    private static final Comparator<Session> DISPLAY_ORDER = Comparator
            .comparing((Session session) -> session.getGroup().toString())
            .thenComparing(Session::getWeek);

    private final ObservableList<Session> internalList = FXCollections.observableArrayList();
    private final ObservableList<Session> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** Returns true if a session with the same group and teaching week exists. */
    public boolean contains(Session toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameSession);
    }

    /** Adds {@code toAdd} and keeps sessions ordered by group and teaching week. */
    public void add(Session toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateSessionException();
        }
        internalList.add(toAdd);
        internalList.sort(DISPLAY_ORDER);
    }

    /** Returns the session for {@code group} and {@code week}, if one exists. */
    public Optional<Session> find(Group group, Week week) {
        requireAllNonNull(group, week);
        return internalList.stream().filter(session -> session.isFor(group, week)).findFirst();
    }

    /** Replaces the list with {@code sessions}. */
    public void setSessions(List<Session> sessions) {
        requireAllNonNull(sessions);
        if (!sessionsAreUnique(sessions)) {
            throw new DuplicateSessionException();
        }
        internalList.setAll(sessions);
        internalList.sort(DISPLAY_ORDER);
    }

    /** Returns an unmodifiable view backed by this list. */
    public ObservableList<Session> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Session> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof UniqueSessionList otherList && internalList.equals(otherList.internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

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
