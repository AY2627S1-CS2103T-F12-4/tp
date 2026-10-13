package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class FindCommandUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void find_throughCommandBox_displaysGlobalCardsAndKeepsIndexesAfterDelete() throws Exception {
        Session firstSession = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        Session secondSession = new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-12"));
        Person firstJohn = new PersonBuilder().withName("John Tan").withMatric("A0000002X")
                .withGroup("T09").withEmail("john.t09@example.com")
                .withAttendance(firstSession, Status.PRESENT).build();
        Person secondJohn = new PersonBuilder().withName("John Tan").withMatric("A0000001X")
                .withGroup("T10").withEmail("john.t10@example.com")
                .withAttendance(secondSession, Status.ABSENT).build();
        Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000003X")
                .withEmail("alice@example.com").build();
        AddressBook book = new AddressBookBuilder().withPerson(firstJohn).withPerson(alice)
                .withPerson(secondJohn).build();
        book.addSession(firstSession);
        book.addSession(secondSession);
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Stage stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    addressBookStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
        try {
            onFxThread(() -> {
                enterCommand(stage, "list grp/T09");
                assertEquals(new Group("T09"), groups(stage).getSelectionModel().getSelectedItem());
                for (String command : List.of("find n/", "find n/John n/Tan", "find n/John grp/T10")) {
                    enterCommand(stage, command);
                    assertEquals(new Group("T09"), groups(stage).getSelectionModel().getSelectedItem());
                    assertEquals(List.of(firstJohn, alice), model.getFilteredPersonList());
                    assertEquals(book, model.getAddressBook());
                }

                enterCommand(stage, "find n/jOhN");
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                assertEquals("All groups", ((Label) stage.getScene().lookup("#groupScope")).getText());
                assertEquals(List.of(
                        List.of("1. ", "John Tan", "A0000001X", "T10", "W1: A"),
                        List.of("2. ", "John Tan", "A0000002X", "T09", "W1: P")), visibleRows(stage));
                assertEquals(book, model.getAddressBook());
                assertEquals(book, addressBookStorage.readAddressBook().orElseThrow());

                enterCommand(stage, "mark 1 w/1 s/present");
                assertEquals(List.of(
                        List.of("1. ", "John Tan", "A0000001X", "T10", "W1: P"),
                        List.of("2. ", "John Tan", "A0000002X", "T09", "W1: P")), visibleRows(stage));
                enterCommand(stage, "delete 1");
                assertEquals(List.of(List.of("1. ", "John Tan", "A0000002X", "T09", "W1: P")),
                        visibleRows(stage));
                assertEquals(List.of(firstJohn, alice), model.getAddressBook().getPersonList());
                assertTrue(model.hasGroup(new Group("T10")));

                enterCommand(stage, "list grp/T09");
                enterCommand(stage, "find n/Nobody");
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                assertTrue(visibleRows(stage).isEmpty());
                assertTrue(model.getFilteredPersonList().isEmpty());
                enterCommand(stage, "find n/Alice");
                assertEquals(List.of(List.of("1. ", "Alice Tan", "A0000003X", "T09", "W1: \u2014")),
                        visibleRows(stage));
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
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

    private static ListView<?> groups(Stage stage) {
        return (ListView<?>) stage.getScene().lookup("#groupListView");
    }

    private static List<List<String>> visibleRows(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
        return list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty() && cell.getGraphic() != null)
                .sorted(Comparator.comparingInt(cell -> cell.getIndex()))
                .map(cell -> {
                    Node card = cell.getGraphic();
                    FlowPane attendance = (FlowPane) card.lookup("#attendance");
                    return List.of(label(card, "id"), label(card, "name"), label(card, "matric"),
                            label(card, "group"), ((Label) attendance.getChildren().getFirst()).getText());
                }).toList();
    }

    private static String label(Node card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }
}
