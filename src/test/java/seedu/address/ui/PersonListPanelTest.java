package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.testutil.PersonBuilder;

public class PersonListPanelTest {
    @Test
    public void activeGroup_changesUpdateLabelAndListKeepsObservableSource() throws Exception {
        onFxThread(() -> {
            ObservableList<Person> people = FXCollections.observableArrayList(ALICE);
            ReadOnlyObjectWrapper<Group> activeGroup = new ReadOnlyObjectWrapper<>();
            PersonListPanel panel = new PersonListPanel(people, FXCollections.observableArrayList(),
                    activeGroup.getReadOnlyProperty());
            Label scope = (Label) panel.getRoot().lookup("#groupScope");
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            assertSame(people, list.getItems());
            assertEquals("All groups", scope.getText());
            activeGroup.set(new Group("T09"));
            assertEquals("Tutorial group T09", scope.getText());
            activeGroup.set(null);
            assertEquals("All groups", scope.getText());
            return null;
        });
    }

    @Test
    public void cell_reusedForEmptyRow_clearsStudentGraphic() throws Exception {
        onFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE),
                    FXCollections.observableArrayList(T09_WEEK_1), new ReadOnlyObjectWrapper<Group>());
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateIndex(0);
            cell.updateItem(ALICE, false);
            assertEquals("1. ", ((Label) cell.getGraphic().lookup("#id")).getText());
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
            cell.updateItem(ALICE, false);
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            return null;
        });
    }

    @Test
    public void sessions_addedAfterDisplay_refreshVisibleAttendanceCells() throws Exception {
        ObservableList<Session> sessions = FXCollections.observableArrayList();
        Stage stage = onFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE), sessions,
                    new ReadOnlyObjectWrapper<Group>());
            Stage window = new Stage();
            window.setScene(new Scene(panel.getRoot(), 600, 400));
            window.show();
            return window;
        });
        try {
            onFxThread(() -> {
                assertEquals(List.of("No sessions yet"), visibleAttendance(stage));
                sessions.add(T09_WEEK_1);
                return null;
            });
            onFxThread(() -> {
                assertEquals(List.of("W1: \u2014"), visibleAttendance(stage));
                return null;
            });
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    public void scrollTo_studentFarDownLongList_showsAndHighlightsTheirCard() throws Exception {
        ObservableList<Person> people = FXCollections.observableArrayList();
        for (int i = 0; i < 40; i++) {
            people.add(new PersonBuilder().withName("Student " + (char) ('A' + i / 26) + (char) ('A' + i % 26))
                    .withMatric(String.format("A%07dA", i)).withEmail("student" + i + "@u.nus.edu").build());
        }
        Person lastStudent = people.get(39);
        PersonListPanel panel = onFxThread(() -> new PersonListPanel(people, FXCollections.observableArrayList(),
                new ReadOnlyObjectWrapper<Group>()));
        Stage stage = onFxThread(() -> {
            Stage window = new Stage();
            window.setScene(new Scene(panel.getRoot(), 600, 400));
            window.show();
            return window;
        });
        try {
            onFxThread(() -> {
                panel.scrollTo(lastStudent);
                return null;
            });
            onFxThread(() -> {
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                ListView<?> list = (ListView<?>) stage.getScene().getRoot().lookup("#personListView");
                assertEquals(39, list.getSelectionModel().getSelectedIndex());
                assertTrue(stage.getScene().getRoot().lookupAll(".list-cell").stream()
                        .map(node -> (ListCell<?>) node)
                        .anyMatch(cell -> cell.getIndex() == 39 && cell.isVisible() && !cell.isEmpty()),
                        "Expected the last student's card to be drawn");

                // a student who is not displayed leaves the list as it is
                panel.scrollTo(ALICE);
                assertEquals(39, list.getSelectionModel().getSelectedIndex());

                panel.clearSelection();
                assertEquals(-1, list.getSelectionModel().getSelectedIndex());
                return null;
            });
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    private static List<String> visibleAttendance(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        List<FlowPane> strips = stage.getScene().getRoot().lookupAll("#attendance").stream()
                .map(node -> (FlowPane) node).toList();
        assertTrue(!strips.isEmpty(), "Expected a rendered student card");
        return strips.getFirst().getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }
}
