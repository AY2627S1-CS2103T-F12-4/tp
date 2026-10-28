package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.FxTestUtil.waitForRenderingCycles;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests, in a real window, that a newly added student's card is scrolled fully into view.
 */
public class AddCommandUiTest {

    private static final int RENDERING_CYCLES_TO_WAIT = 4;

    @TempDir
    public Path temporaryFolder;

    private Stage stage;

    @Test
    public void add_longListAndGroupSwitch_scrollsNewCardFullyIntoView() throws Exception {
        // T09 has many students with 13 sessions each, so its cards are much taller than T10's
        AddressBookBuilder builder = new AddressBookBuilder();
        for (int i = 10; i < 40; i++) {
            builder.withPerson(new PersonBuilder().withName("Student " + (char) ('A' + i % 26))
                    .withMatric(String.format("A01000%02dA", i)).withEmail("student" + i + "@u.nus.edu")
                    .withGroup(i % 3 == 0 ? "T10" : "T09").build());
        }
        AddressBook book = builder.build();
        for (int week = 1; week <= 13; week++) {
            book.addSession(new Session(new Group("T09"), new Week(week),
                    new SessionDate(String.format("2026-08-%02d", week + 10))));
        }
        openWindow(book);

        try {
            enterCommand("list");
            // first, middle and last positions in the full list
            enterCommand("add n/First Added e/first@u.nus.edu m/A0000001A grp/T09");
            assertAddedCardFullyVisible("A0000001A");
            enterCommand("add n/Middle Added e/middle@u.nus.edu m/A0100025Z grp/T09");
            assertAddedCardFullyVisible("A0100025Z");
            enterCommand("add n/Last Added e/last@u.nus.edu m/A0999999A grp/T09");
            assertAddedCardFullyVisible("A0999999A");

            // an empty search, so the view switches to the new student's group
            enterCommand("find n/Nobody");
            enterCommand("add n/Highest Matric e/highest@u.nus.edu m/A0999999Z grp/T10");
            assertAddedCardFullyVisible("A0999999Z");
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    public void failedCommandAfterAdd_clearsHighlight() throws Exception {
        openWindow(new AddressBookBuilder().withPerson(new PersonBuilder().build()).build());
        try {
            enterCommand("add n/Second Student e/second@u.nus.edu m/A0999999Z grp/T09");
            waitForRenderingCycles(RENDERING_CYCLES_TO_WAIT);
            assertEquals(1, onFxThread(() -> personList().getSelectionModel().getSelectedIndex()));

            enterCommand("delete 999");
            waitForRenderingCycles(RENDERING_CYCLES_TO_WAIT);
            assertEquals(-1, onFxThread(() -> personList().getSelectionModel().getSelectedIndex()));
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    private void openWindow(AddressBook book) throws Exception {
        ModelManager model = new ModelManager(book, new UserPrefs());
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        stage = onFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    addressBookStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            Stage primaryStage = window.getPrimaryStage();
            primaryStage.setWidth(1000);
            primaryStage.setHeight(700);
            return primaryStage;
        });
    }

    private void enterCommand(String command) throws Exception {
        onFxThread(() -> {
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText(command);
            input.fireEvent(new ActionEvent());
            return null;
        });
    }

    private ListView<?> personList() {
        return (ListView<?>) stage.getScene().lookup("#personListView");
    }

    /**
     * Asserts that the highlighted card is the student with {@code matric}, and that the whole card lies
     * within the visible part of the list. Being drawn is not enough, as a drawn card can be clipped.
     */
    private void assertAddedCardFullyVisible(String matric) throws Exception {
        waitForRenderingCycles(RENDERING_CYCLES_TO_WAIT);
        onFxThread(() -> {
            ListView<?> list = personList();
            int selectedIndex = list.getSelectionModel().getSelectedIndex();
            ListCell<?> selectedCell = list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                    .filter(cell -> cell.getIndex() == selectedIndex && !cell.isEmpty() && cell.isVisible())
                    .findFirst().orElseThrow(() -> new AssertionError("The highlighted card is not drawn"));
            Node card = selectedCell.getGraphic();
            assertEquals(matric, ((javafx.scene.control.Label) card.lookup("#matric")).getText());

            Node viewport = list.lookup(".clipped-container");
            Bounds visibleArea = viewport.localToScene(viewport.getBoundsInLocal());
            Bounds cardArea = selectedCell.localToScene(selectedCell.getBoundsInLocal());
            assertTrue(cardArea.getMinY() >= visibleArea.getMinY() - 0.5
                    && cardArea.getMaxY() <= visibleArea.getMaxY() + 0.5,
                    "Card of " + matric + " spans y=" + cardArea.getMinY() + ".." + cardArea.getMaxY()
                            + " but the visible list spans y=" + visibleArea.getMinY() + ".."
                            + visibleArea.getMaxY());
            return null;
        });
    }
}
