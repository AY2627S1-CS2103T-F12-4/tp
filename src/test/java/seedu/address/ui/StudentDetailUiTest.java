package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class StudentDetailUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void detailPanel_openAndClose_leavesTheDisplayedListUntouched() throws Exception {
        Session session = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000001X")
                .withGroup("T09").withEmail("alice@example.com").withComment("attentive")
                .withAttendance(session, Status.PRESENT).build();
        Person bob = new PersonBuilder().withName("Bob Tan").withMatric("A0000002X")
                .withGroup("T09").withEmail("bob@example.com").build();
        AddressBook book = new AddressBookBuilder().withPerson(alice).withPerson(bob).build();
        book.addSession(session);
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage jsonStorage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        jsonStorage.saveAddressBook(book);
        StorageManager storage = new StorageManager(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));

        Stage stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    jsonStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
        try {
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                input.setText("list grp/T09");
                input.fireEvent(new ActionEvent());

                List<Person> before = List.copyOf(model.getFilteredPersonList());
                Group activeBefore = model.activeGroupProperty().get();
                assertFalse(detailShowing(stage));

                clickRow(stage, 0);
                assertTrue(detailShowing(stage));
                assertEquals("Alice Tan", detailText(stage, "studentName"));
                assertEquals("Row 1 in this list", detailText(stage, "rowNumber"));

                // The panel opens over the roster only, so commands and their results can still be read
                assertFalse(overlapsDetailPanel(stage, "#commandTextField"));
                assertFalse(overlapsDetailPanel(stage, "#resultDisplay"));

                // Opening the panel changes neither the roster, the displayed rows nor the view
                assertEquals(before, model.getFilteredPersonList());
                assertEquals(activeBefore, model.activeGroupProperty().get());
                assertEquals(book, model.getAddressBook());
                assertEquals("1. ", firstRowIndex(stage));

                // Choosing another row swaps the contents rather than opening a second panel
                clickRow(stage, 1);
                assertEquals("Bob Tan", detailText(stage, "studentName"));
                assertEquals("Row 2 in this list", detailText(stage, "rowNumber"));
                assertEquals(before, model.getFilteredPersonList());

                press(stage, KeyCode.ESCAPE);
                assertFalse(detailShowing(stage));
                assertEquals(before, model.getFilteredPersonList());
                assertEquals(activeBefore, model.activeGroupProperty().get());
                assertEquals(book, model.getAddressBook());
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
    public void detailPanel_commandChangesTheStudent_panelFollowsOrCloses() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000001X")
                .withGroup("T09").withEmail("alice@example.com").build();
        AddressBook book = new AddressBookBuilder().withPerson(alice).build();
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage jsonStorage = new JsonAddressBookStorage(temporaryFolder.resolve("roster2.json"));
        jsonStorage.saveAddressBook(book);
        StorageManager storage = new StorageManager(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences2.json")));

        Stage stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    jsonStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
        try {
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                clickRow(stage, 0);
                assertTrue(detailShowing(stage));

                // A comment on the open student is reflected rather than left stale
                input.setText("comment 1 c/attentive");
                input.fireEvent(new ActionEvent());
                assertTrue(detailShowing(stage));
                assertEquals("Alice Tan", detailText(stage, "studentName"));

                // An edit of the matriculation number changes what identifies the open student,
                // so the panel has to follow the student the command reported instead of closing
                input.setText("edit 1 m/A0000003X");
                input.fireEvent(new ActionEvent());
                assertTrue(detailShowing(stage));
                assertEquals("Alice Tan", detailText(stage, "studentName"));
                assertEquals("A0000003X", detailText(stage, "studentMatric"));

                // Once the student is no longer displayed there is nothing to show
                input.setText("delete 1");
                input.fireEvent(new ActionEvent());
                assertFalse(detailShowing(stage));
                return null;
            });
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    private static boolean detailShowing(Stage stage) {
        return stage.getScene().lookup("#detailPane").isVisible();
    }

    private static boolean overlapsDetailPanel(Stage stage, String selector) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        Node panel = stage.getScene().lookup("#detailPane");
        Node other = stage.getScene().lookup(selector);
        // Layout bounds leave out the panel's drop shadow, which may fall across its neighbours
        return panel.localToScene(panel.getLayoutBounds()).intersects(other.localToScene(other.getLayoutBounds()));
    }

    private static String detailText(Stage stage, String id) {
        return ((Label) stage.getScene().lookup("#" + id)).getText();
    }

    private static String firstRowIndex(Stage stage) {
        return ((Label) stage.getScene().lookup("#personListView").lookup("#id")).getText();
    }

    private static void press(Stage stage, KeyCode code) {
        stage.getScene().lookup("#personListView").fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void clickRow(Stage stage, int index) {
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
        list.applyCss();
        list.layout();
        ListCell<?> row = list.lookupAll(".list-cell").stream()
                .map(node -> (ListCell<?>) node)
                .filter(cell -> cell.getIndex() == index && !cell.isEmpty())
                .findFirst().orElseThrow();
        row.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                false, false, false, false, false, false, false, true, false, true, null));
    }
}
