package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.ui.MainWindowTestFixture.enterCommand;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class FindCommandUiTest {
    @TempDir
    public Path temporaryFolder;

    private final Session firstSession = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
    private final Session secondSession = new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-12"));
    private final Person firstJohn = new PersonBuilder().withName("John Tan").withMatric("A0000002X")
            .withGroup("T09").withEmail("john.t09@example.com").withAttendance(firstSession, Status.PRESENT).build();
    private final Person secondJohn = new PersonBuilder().withName("John Tan").withMatric("A0000001X")
            .withGroup("T10").withEmail("john.t10@example.com").withAttendance(secondSession, Status.ABSENT).build();
    private final Person alice = new PersonBuilder().withName("Alice Tan").withMatric("A0000003X")
            .withEmail("alice@example.com").build();

    @Test
    public void find_invalidQueries_preservesGroupViewAndRecords() throws Exception {
        AddressBook book = createRoster();
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                enterCommand(stage, "list grp/T09");
                assertEquals("T09", groups(stage).getSelectionModel().getSelectedItem());
                for (String command : List.of("find n/", "find n/John n/Tan", "find n/John e/invalid")) {
                    enterCommand(stage, command);
                    assertEquals("T09", groups(stage).getSelectionModel().getSelectedItem());
                    assertEquals(List.of(firstJohn, alice), fixture.model().getFilteredPersonList());
                    assertEquals(book, fixture.model().getAddressBook());
                }
                assertEquals(book, fixture.storage().readAddressBook().orElseThrow());
                return null;
            });
        }
    }

    @Test
    public void find_filters_displaysGlobalCardsAndClearsGroupSelection() throws Exception {
        AddressBook book = createRoster();
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                enterCommand(stage, "list grp/T09");
                enterCommand(stage, "find n/jOhN");
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                assertEquals("All groups", ((Label) stage.getScene().lookup("#groupScope")).getText());
                assertEquals(List.of(
                        List.of("1. ", "John Tan", "A0000001X", "T10", "W1: \u2717"),
                        List.of("2. ", "John Tan", "A0000002X", "T09", "W1: \u2713")), visibleRows(stage));
                assertEquals(book, fixture.model().getAddressBook());
                assertEquals(book, fixture.storage().readAddressBook().orElseThrow());
                enterCommand(stage, "find n/John grp/T10");
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                assertEquals(List.of(List.of("1. ", "John Tan", "A0000001X", "T10", "W1: \u2717")),
                        visibleRows(stage));
                enterCommand(stage, "find m/a0000002x");
                assertEquals(List.of(List.of("1. ", "John Tan", "A0000002X", "T09", "W1: \u2713")),
                        visibleRows(stage));
                enterCommand(stage, "list grp/T09");
                enterCommand(stage, "find n/Nobody");
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                assertTrue(visibleRows(stage).isEmpty());
                assertTrue(fixture.model().getFilteredPersonList().isEmpty());
                enterCommand(stage, "find n/Alice");
                assertEquals(List.of(List.of("1. ", "Alice Tan", "A0000003X", "T09", "W1: \u2014")),
                        visibleRows(stage));
                assertNull(groups(stage).getSelectionModel().getSelectedItem());
                return null;
            });
        }
    }

    @Test
    public void find_markAndDelete_useDisplayedIndexes() throws Exception {
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(createRoster(), temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                enterCommand(stage, "find n/John");
                enterCommand(stage, "mark 1 w/1 s/present");
                assertEquals(List.of(
                        List.of("1. ", "John Tan", "A0000001X", "T10", "W1: \u2713"),
                        List.of("2. ", "John Tan", "A0000002X", "T09", "W1: \u2713")), visibleRows(stage));
                enterCommand(stage, "delete 1");
                assertEquals(List.of(List.of("1. ", "John Tan", "A0000002X", "T09", "W1: \u2713")),
                        visibleRows(stage));
                assertEquals(List.of(firstJohn, alice), fixture.model().getAddressBook().getPersonList());
                assertTrue(fixture.model().hasGroup(new Group("T10")));
                return null;
            });
        }
    }

    private AddressBook createRoster() {
        AddressBook book = new AddressBookBuilder().withPerson(firstJohn).withPerson(alice)
                .withPerson(secondJohn).build();
        book.addSession(firstSession);
        book.addSession(secondSession);
        return book;
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
