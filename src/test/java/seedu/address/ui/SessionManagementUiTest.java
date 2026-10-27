package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class SessionManagementUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void commandsThroughWindow_refreshAttendanceWithoutLosingSearchResults() throws Exception {
        Session first = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        Session second = new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18"));
        Person student = new PersonBuilder().withName("Sarah Tan").withAttendance(first, Status.PRESENT)
                .withAttendance(second, Status.ABSENT).build();
        AddressBook book = new AddressBook();
        book.addGroup(student.getGroup());
        book.addSession(first);
        book.addSession(second);
        book.addPerson(student);
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        Stage stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
        try {
            onFxThread(() -> {
                enterCommand(stage, "find n/Sarah");
                enterCommand(stage, "editsession grp/T09 w/1 d/2026-08-13");
                assertEquals(List.of("W1: \u2713", "W2: \u2717"), attendanceLabels(stage));
                enterCommand(stage, "deletesession grp/T09 w/1");
                assertEquals(List.of("W2: \u2717"), attendanceLabels(stage));
                assertEquals(1, model.getFilteredPersonList().size());
                assertTrue(model.getActiveGroup().isEmpty());
                enterCommand(stage, "undo");
                assertEquals(List.of("W1: \u2713", "W2: \u2717"), attendanceLabels(stage));
                enterCommand(stage, "deletesession grp/T09 w/1");
                enterCommand(stage, "deletesession grp/T09 w/2");
                assertEquals(List.of("No sessions yet"), attendanceLabels(stage));
                return null;
            });
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    private static void enterCommand(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static List<String> attendanceLabels(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
        ListCell<?> cell = list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(candidate -> !candidate.isEmpty() && candidate.getGraphic() != null).findFirst().orElseThrow();
        FlowPane attendance = (FlowPane) cell.getGraphic().lookup("#attendance");
        return attendance.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }
}
