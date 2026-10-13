package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains tests for how {@code PersonCard} shows a person's attendance. ALICE is in T09.
 */
public class PersonCardTest {

    private final Person alice = new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT)
            .withAttendance(T09_WEEK_2, Status.ABSENT).build();

    @Test
    public void getSessionsOfGroup_sessionsOfSeveralGroups_returnsOwnGroupSortedByWeek() {
        List<Session> sessions = List.of(T09_WEEK_2, T10_WEEK_1, T09_WEEK_1);
        assertEquals(List.of(T09_WEEK_1, T09_WEEK_2), PersonCard.getSessionsOfGroup(alice, sessions));
    }

    @Test
    public void getSessionsOfGroup_noSessionOfOwnGroup_returnsEmptyList() {
        assertEquals(List.of(), PersonCard.getSessionsOfGroup(alice, List.of(T10_WEEK_1)));
    }

    @Test
    public void getAttendanceText_present_returnsWeekAndTick() {
        assertEquals("W1 " + PersonCard.TICK, PersonCard.getAttendanceText(alice, T09_WEEK_1));
    }

    @Test
    public void getAttendanceText_absent_returnsWeekAndCross() {
        assertEquals("W2 " + PersonCard.CROSS, PersonCard.getAttendanceText(alice, T09_WEEK_2));
    }

    @Test
    public void getAttendanceText_unmarked_returnsWeekAndDash() {
        assertEquals("W1 " + PersonCard.DASH, PersonCard.getAttendanceText(ALICE, T09_WEEK_1));
    }

    @Test
    public void getAttendanceStyleClass() {
        assertEquals(PersonCard.STYLE_PRESENT, PersonCard.getAttendanceStyleClass(alice, T09_WEEK_1));
        assertEquals(PersonCard.STYLE_ABSENT, PersonCard.getAttendanceStyleClass(alice, T09_WEEK_2));
        assertEquals(PersonCard.STYLE_UNMARKED, PersonCard.getAttendanceStyleClass(ALICE, T09_WEEK_1));
    }

    @Test
    public void constructor_sessionsOfSeveralGroups_showsOwnGroupCellsInWeekOrder() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(alice, 1, List.of(T09_WEEK_2, T10_WEEK_1, T09_WEEK_1));

            assertEquals(List.of("W1 " + PersonCard.TICK, "W2 " + PersonCard.CROSS),
                    getAttendanceTexts(card.getRoot()));
            assertEquals(List.of(PersonCard.STYLE_PRESENT, PersonCard.STYLE_ABSENT),
                    getAttendanceCells(card.getRoot()).stream().map(this::getLastStyleClass).toList());
        });
    }

    @Test
    public void constructor_noSessionOfOwnGroup_showsNoCells() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(alice, 1, List.of(T10_WEEK_1));
            assertEquals(List.of(), getAttendanceTexts(card.getRoot()));
        });
    }

    /**
     * Returns the texts of the attendance cells shown on {@code card}.
     */
    static List<String> getAttendanceTexts(Node card) {
        return getAttendanceCells(card).stream().map(Label::getText).toList();
    }

    private static List<Label> getAttendanceCells(Node card) {
        FlowPane attendances = (FlowPane) card.lookup("#attendances");
        return attendances.getChildren().stream().map(node -> (Label) node).toList();
    }

    private String getLastStyleClass(Label cell) {
        List<String> styleClasses = cell.getStyleClass();
        return styleClasses.get(styleClasses.size() - 1);
    }
}
