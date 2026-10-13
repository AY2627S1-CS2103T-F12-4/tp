package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains tests for how {@code PersonListPanel} keeps the attendance shown on its cards up to date.
 * ALICE is in T09 and DANIEL in T10.
 */
public class PersonListPanelTest {

    private final ObservableList<Person> persons = FXCollections.observableArrayList(ALICE, DANIEL);
    private final ObservableList<Session> sessions = FXCollections.observableArrayList(T09_WEEK_1);

    @Test
    public void constructor_showsEachStudentsOwnGroupSessions() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Scene scene = createScene();
            assertEquals(List.of(List.of("W1 " + PersonCard.DASH), List.of()), getDisplayedAttendance(scene));
        });
    }

    @Test
    public void sessionAdded_redrawsCardsOfThatGroup() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Scene scene = createScene();

            sessions.add(T09_WEEK_2);
            sessions.add(T10_WEEK_1);

            assertEquals(List.of(List.of("W1 " + PersonCard.DASH, "W2 " + PersonCard.DASH),
                    List.of("W1 " + PersonCard.DASH)), getDisplayedAttendance(scene));
        });
    }

    @Test
    public void studentMarked_redrawsTheirCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Scene scene = createScene();

            persons.set(0, new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build());

            assertEquals(List.of(List.of("W1 " + PersonCard.TICK), List.of()), getDisplayedAttendance(scene));
        });
    }

    private Scene createScene() {
        PersonListPanel panel = new PersonListPanel(persons, sessions);
        return new Scene(panel.getRoot(), 500, 600);
    }

    /**
     * Returns the attendance cell texts of each card drawn in {@code scene}, in display order.
     */
    private List<List<String>> getDisplayedAttendance(Scene scene) {
        scene.snapshot(null); // lays out the list, as drawing a frame would
        List<ListCell<?>> cells = new ArrayList<>();
        for (Node node : scene.getRoot().lookupAll(".list-cell")) {
            ListCell<?> cell = (ListCell<?>) node;
            if (cell.isVisible() && !cell.isEmpty() && cell.getGraphic() != null) {
                cells.add(cell);
            }
        }
        cells.sort(Comparator.comparingInt(ListCell::getIndex));
        return cells.stream().map(cell -> PersonCardTest.getAttendanceTexts(cell.getGraphic())).toList();
    }
}
