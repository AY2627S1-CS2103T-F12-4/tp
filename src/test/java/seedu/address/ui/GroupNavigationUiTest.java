package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
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

public class GroupNavigationUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void navigation_clickAndKeyboard_preservesDraftAndRecordsAndRefreshesIndexes() throws Exception {
        Session session = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000002X")
                .withGroup("T09").withEmail("alice@example.com").withAttendance(session, Status.PRESENT).build();
        Person bob = new PersonBuilder().withName("Bob Tan").withMatric("A0000001X")
                .withGroup("T10").withEmail("bob@example.com").build();
        AddressBook book = new AddressBookBuilder().withPerson(alice).withPerson(bob).build();
        book.addSession(session);
        book.addGroup(new Group("T11"));
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage jsonStorage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        AtomicBoolean failSave = new AtomicBoolean();
        StorageManager storage = new StorageManager(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (failSave.get()) {
                    throw new IOException("read-only test");
                }
                super.saveAddressBook(addressBook);
            }
        };
        Stage stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    jsonStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            return window.getPrimaryStage();
        });
        try {
            onFxThread(() -> {
                ListView<?> groups = (ListView<?>) stage.getScene().lookup("#groupListView");
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                assertEquals("All students", groups.getSelectionModel().getSelectedItem());
                input.setText("mark 1 w/1 s/");
                click(groups, 1, MouseButton.PRIMARY);
                assertEquals("mark 1 w/1 s/", input.getText());
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertEquals("Tutorial group T09", ((Label) stage.getScene().lookup("#groupScope")).getText());
                assertEquals("T09", groups.getSelectionModel().getSelectedItem());
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                Label rowIndex = (Label) stage.getScene().lookup("#personListView").lookup("#id");
                assertEquals("1. ", rowIndex.getText());
                assertEquals(Color.WHITE, cell(groups, 1).getTextFill());

                press(groups, KeyCode.DOWN);
                assertEquals("T10", groups.getSelectionModel().getSelectedItem());
                assertEquals(List.of(alice), model.getFilteredPersonList());
                press(groups, KeyCode.ENTER);
                assertEquals(List.of(bob), model.getFilteredPersonList());
                assertEquals("mark 1 w/1 s/", input.getText());
                click(groups, 0, MouseButton.PRIMARY);
                assertEquals(List.of(bob, alice), model.getFilteredPersonList());
                assertEquals("All students", groups.getSelectionModel().getSelectedItem());
                press(groups, KeyCode.UP);
                assertEquals(List.of(bob, alice), model.getFilteredPersonList());
                assertEquals(book, model.getAddressBook());
                assertEquals(book, jsonStorage.readAddressBook().orElseThrow());

                click(groups, 3, MouseButton.PRIMARY);
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertEquals("Tutorial group T11 has no students yet.", feedback(stage));
                click(groups, 1, MouseButton.SECONDARY);
                assertEquals(new Group("T11"), model.activeGroupProperty().get());
                groups.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                        false, false, false, false, false, false, false, true, false, true, null));
                assertEquals(new Group("T11"), model.activeGroupProperty().get());

                input.setText("list grp/T10");
                input.fireEvent(new ActionEvent());
                assertEquals("T10", groups.getSelectionModel().getSelectedItem());
                input.setText("find n/Tan");
                input.fireEvent(new ActionEvent());
                assertNull(groups.getSelectionModel().getSelectedItem());
                assertEquals(List.of(bob, alice), model.getFilteredPersonList());
                click(groups, 0, MouseButton.PRIMARY);
                assertEquals("All students", groups.getSelectionModel().getSelectedItem());
                input.setText("find n/Nobody");
                input.fireEvent(new ActionEvent());
                assertNull(groups.getSelectionModel().getSelectedItem());
                assertTrue(model.getFilteredPersonList().isEmpty());
                click(groups, 1, MouseButton.PRIMARY);
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertEquals(book, model.getAddressBook());

                failSave.set(true);
                click(groups, 0, MouseButton.PRIMARY);
                assertTrue(feedback(stage).contains("Could not save data"));
                assertEquals("All students", groups.getSelectionModel().getSelectedItem());
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

    private static void press(ListView<?> list, KeyCode key) {
        list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
    }

    private static ListCell<?> cell(ListView<?> list, int index) {
        list.applyCss();
        list.layout();
        return list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(row -> row.getIndex() == index).findFirst().orElseThrow();
    }

    private static void click(ListView<?> list, int index, MouseButton button) {
        cell(list, index).fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, button, 1,
                false, false, false, false, false, false, false, true, false, true, null));
    }

    private static String feedback(Stage stage) {
        return ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
    }
}
