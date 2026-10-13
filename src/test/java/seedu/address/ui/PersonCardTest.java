package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @Test
    public void constructor_rendersStudentDetailsAndAttendanceInWeekOrder() throws Exception {
        onFxThread(() -> {
            Person student = new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build();
            PersonCard card = new PersonCard(student, 3, List.of(T09_WEEK_2, T10_WEEK_1, T09_WEEK_1));
            assertEquals("3. ", label(card, "id"));
            assertEquals(student.getName().fullName, label(card, "name"));
            assertEquals(student.getMatric().value, label(card, "matric"));
            assertEquals("T09", label(card, "group"));
            assertEquals(student.getEmail().value, label(card, "email"));
            assertEquals(List.of("W1: P", "W2: \u2014"), attendanceLabels(card));

            Person updated = new PersonBuilder(student).withAttendance(T09_WEEK_2, Status.ABSENT).build();
            assertEquals(List.of("W1: P", "W2: A"),
                    attendanceLabels(new PersonCard(updated, 3, List.of(T09_WEEK_2, T09_WEEK_1))));
            return null;
        });
    }

    @Test
    public void constructor_withoutSessionsForStudentGroup_showsNoSessionsMessage() throws Exception {
        onFxThread(() -> {
            assertEquals(List.of("No sessions yet"),
                    attendanceLabels(new PersonCard(ALICE, 1, List.of())));
            assertEquals(List.of("No sessions yet"),
                    attendanceLabels(new PersonCard(ALICE, 1, List.of(T10_WEEK_1))));
            return null;
        });
    }

    private static String label(PersonCard card, String id) {
        return ((Label) card.getRoot().lookup("#" + id)).getText();
    }

    private static List<String> attendanceLabels(PersonCard card) {
        FlowPane attendance = (FlowPane) card.getRoot().lookup("#attendance");
        return attendance.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }
}
