package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.FxTestUtil.waitForRenderingCycles;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class UiComponentsIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void feedback_commandsAndUndoShortcuts_updateBothBannerAndInputStatus() throws Exception {
        WindowFixture fixture = createWindow(false);
        try {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                enter(stage, "edit 1 n/Alex Lim");
                assertFeedback(stage, false);
                enter(stage, "unknown");
                enter(stage, "unknown");
                assertFeedback(stage, true);
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                result.requestFocus();
                result.fireEvent(shortcutZ());
                assertFeedback(stage, false);
                assertEquals("Alex Tan", fixture.model().getAddressBook().getPersonList().getFirst()
                        .getName().fullName);
                result.fireEvent(shortcutZ());
                assertFeedback(stage, true);
                input(stage).setText("list");
                assertFalse(stage.getScene().lookup("#commandBox").getStyleClass().contains("error"));
                enter(stage, "list");
                assertFeedback(stage, false);
                return null;
            });
            saveScreenshotAfterRendering(fixture.stage(), "feedback-success.png");
            onFxThread(() -> {
                enter(fixture.stage(), "unknown");
                return null;
            });
            saveScreenshotAfterRendering(fixture.stage(), "feedback-error.png");
        } finally {
            close(fixture);
        }
    }

    @Test
    public void filteringCommentsAndUndo_refreshStudentCountAndPreserveMetadata() throws Exception {
        WindowFixture fixture = createWindow(false);
        try {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                assertEquals("3 students", label(stage, "studentCount").getText());
                enter(stage, "find n/Alex");
                assertEquals("1 student", label(stage, "studentCount").getText());
                assertEquals("Needs worksheet", ((Label) firstCard(stage).lookup("#comment")).getText());
                enter(stage, "comment 1 c-/Updated note");
                assertEquals("Updated note", ((Label) firstCard(stage).lookup("#comment")).getText());
                enter(stage, "comment 1 c-/");
                assertFalse(firstCard(stage).lookup("#comment").isManaged());
                enter(stage, "undo");
                assertEquals("Updated note", ((Label) firstCard(stage).lookup("#comment")).getText());
                enter(stage, "find n/Nobody");
                assertEquals("0 students", label(stage, "studentCount").getText());
                enter(stage, "list");
                enter(stage, "clear");
                assertEquals("0 students", label(stage, "studentCount").getText());
                enter(stage, "undo");
                assertEquals("3 students", label(stage, "studentCount").getText());
                return null;
            });
        } finally {
            close(fixture);
        }
    }

    @Test
    public void layout_minimumAndLargerWindows_keepLongTextAndAllWeeksInsideCards() throws Exception {
        WindowFixture fixture = createWindow(true);
        try {
            for (int[] size : List.of(new int[]{740, 600}, new int[]{1000, 700}, new int[]{1400, 900})) {
                onFxThread(() -> {
                    fixture.stage().setWidth(size[0]);
                    fixture.stage().setHeight(size[1]);
                    return null;
                });
                waitForRenderingCycles(3);
                onFxThread(() -> {
                    Stage stage = fixture.stage();
                    Node card = firstCard(stage);
                    Bounds cardBounds = card.localToScene(card.getBoundsInLocal());
                    FlowPane attendance = (FlowPane) card.lookup("#attendance");
                    assertEquals(13, attendance.getChildren().size());
                    for (Node cell : attendance.getChildren()) {
                        assertWithin(cardBounds, cell.localToScene(cell.getBoundsInLocal()));
                    }
                    for (String field : List.of("name", "email", "comment")) {
                        Label value = (Label) card.lookup("#" + field);
                        assertWithin(cardBounds, value.localToScene(value.getBoundsInLocal()));
                        assertEquals(value.getText(), ((Text) value.lookup(".text")).getText());
                    }
                    FlowPane tags = (FlowPane) card.lookup("#tags");
                    for (Node node : tags.getChildren()) {
                        Label tag = (Label) node;
                        assertTrue(tag.getWidth() <= 165);
                        assertEquals(tag.getText(), tag.getTooltip().getText());
                    }
                    ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
                    Bounds viewport = students.lookup(".clipped-container")
                            .localToScene(students.lookup(".clipped-container").getBoundsInLocal());
                    assertTrue(cardBounds.getWidth() <= viewport.getWidth() + 1,
                            "Student cards must fit the viewport without hiding weeks off screen");
                    assertTrue(students.getHeight() > 80, "The roster must remain usable at the minimum size");
                    saveScreenshot(stage, "layout-" + size[0] + "x" + size[1] + ".png");
                    return null;
                });
            }
        } finally {
            close(fixture);
        }
    }

    @Test
    public void horizontalGroupNavigation_manyGroups_keepsSelectionAndDraftInput() throws Exception {
        WindowFixture fixture = createWindow(false);
        try {
            onFxThread(() -> {
                for (int i = 1; i <= 20; i++) {
                    Group group = new Group(String.format("T%02d", i));
                    if (!fixture.model().hasGroup(group)) {
                        fixture.model().addGroup(group);
                    }
                }
                fixture.stage().setWidth(740);
                fixture.stage().setHeight(600);
                return null;
            });
            waitForRenderingCycles(3);
            onFxThread(() -> {
                Stage stage = fixture.stage();
                input(stage).setText("unfinished command");
                ListView<?> groups = (ListView<?>) stage.getScene().lookup("#groupListView");
                groups.getSelectionModel().selectLast();
                groups.scrollTo(groups.getItems().size() - 1);
                groups.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER,
                        false, false, false, false));
                assertEquals(new Group("T20"), fixture.model().getActiveGroup().orElseThrow());
                assertEquals("unfinished command", input(stage).getText());
                assertEquals("0 students", label(stage, "studentCount").getText());
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                for (Node node : groups.lookupAll(".list-cell")) {
                    if (node instanceof ListCell<?> cell && !cell.isEmpty()) {
                        assertTrue(cell.getHeight() >= 20, "Group names must fit above the scrollbar");
                    }
                }
                saveScreenshot(stage, "many-groups.png");
                return null;
            });
        } finally {
            close(fixture);
        }
    }

    private WindowFixture createWindow(boolean longText) throws Exception {
        WindowFixture fixture = onFxThread(() -> {
            AddressBook book = new AddressBook();
            Group firstGroup = new Group("T09");
            Group secondGroup = new Group("T10");
            book.addGroup(firstGroup);
            book.addGroup(secondGroup);
            for (int week = 1; week <= 13; week++) {
                book.addSession(new Session(firstGroup, new Week(week),
                        new SessionDate(LocalDate.of(2026, 8, 11).plusWeeks(week - 1).toString())));
            }
            PersonBuilder first = new PersonBuilder().withName(longText ? "Alexandra ".repeat(7).strip() : "Alex Tan")
                    .withMatric("A0000001X").withEmail(longText ? "verylongemail".repeat(5) + "@example.com"
                            : "alex@example.com").withGroup("T09")
                    .withComment(longText ? "Needs worksheet and attendance follow up. ".repeat(6).substring(0, 200)
                            : "Needs worksheet")
                    .withTags("classrep", longText ? "x".repeat(100) : "exchange");
            first.withAttendance(book.getSessionList().getFirst(), Status.PRESENT);
            book.addPerson(first.build());
            book.addPerson(new PersonBuilder().withName("Bernice Tan").withMatric("A0000002X")
                    .withEmail("bernice@example.com").withGroup("T09").build());
            book.addPerson(new PersonBuilder().withName("Chris Tan").withMatric("A0000003X")
                    .withEmail("chris@example.com").withGroup("T10").build());
            ModelManager model = new ModelManager(book, new UserPrefs());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            Stage stage = window.getPrimaryStage();
            stage.setWidth(1000);
            stage.setHeight(700);
            return new WindowFixture(stage, model);
        });
        waitForRenderingCycles(3);
        return fixture;
    }

    private static void assertFeedback(Stage stage, boolean error) {
        assertEquals(error ? "Command not run" : "Command completed", label(stage, "resultTitle").getText());
        assertEquals(error ? "!" : "\u2713", label(stage, "resultIcon").getText());
        assertEquals(error, stage.getScene().lookup("#commandBox").getStyleClass().contains("error"));
        assertEquals(!error, stage.getScene().lookup("#commandBox").getStyleClass().contains("success"));
        assertEquals(error, input(stage).getStyleClass().contains("error"));
        assertEquals(1, stage.getScene().lookup("#placeHolder").getStyleClass().stream()
                .filter(value -> value.equals(error ? "result-error" : "result-success")).count());
    }

    private static void enter(Stage stage, String text) {
        input(stage).setText(text);
        input(stage).fireEvent(new ActionEvent());
    }

    private static TextField input(Stage stage) {
        return (TextField) stage.getScene().lookup("#commandTextField");
    }

    private static Label label(Stage stage, String id) {
        return (Label) stage.getScene().lookup("#" + id);
    }

    private static Node firstCard(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
        return list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty() && cell.getIndex() == 0 && cell.getGraphic() != null)
                .map(ListCell::getGraphic).findFirst().orElseThrow();
    }

    private static void assertWithin(Bounds parent, Bounds child) {
        assertTrue(child.getMinX() >= parent.getMinX() - 1 && child.getMaxX() <= parent.getMaxX() + 1,
                "Content must fit within the card width: " + child + " vs " + parent);
        assertTrue(child.getMinY() >= parent.getMinY() - 1 && child.getMaxY() <= parent.getMaxY() + 1,
                "Content must fit within the card height: " + child + " vs " + parent);
    }

    private static KeyEvent shortcutZ() {
        boolean mac = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.Z, false, !mac, false, mac);
    }

    private static void saveScreenshotAfterRendering(Stage stage, String name) throws Exception {
        waitForRenderingCycles(3);
        onFxThread(() -> {
            ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
            List<? extends ListCell<?>> rows = students.lookupAll(".list-cell").stream()
                    .map(node -> (ListCell<?>) node).filter(cell -> !cell.isEmpty() && cell.getGraphic() != null)
                    .sorted((first, second) -> Integer.compare(first.getIndex(), second.getIndex())).toList();
            for (ListCell<?> row : rows) {
                Node card = row.getGraphic();
                assertWithin(row.localToScene(row.getLayoutBounds()), card.localToScene(card.getBoundsInLocal()));
                Label comment = (Label) card.lookup("#comment");
                if (comment.isManaged()) {
                    Bounds commentBounds = comment.localToScene(comment.getBoundsInLocal());
                    assertWithin(card.localToScene(card.getBoundsInLocal()), commentBounds);
                    assertTrue(comment.getHeight() >= comment.prefHeight(comment.getWidth()) - 1,
                            "Comments must have room to wrap without being clipped");
                    for (String field : List.of("name", "email", "attendance", "tags")) {
                        Node details = card.lookup("#" + field);
                        assertTrue(details.localToScene(details.getBoundsInLocal()).getMaxY()
                                <= commentBounds.getMinY() + 1, "Comments must stay below " + field + ": "
                                        + details.localToScene(details.getBoundsInLocal()) + " vs " + commentBounds);
                    }
                }
            }
            saveScreenshot(stage, name);
            return null;
        });
    }

    private static void saveScreenshot(Stage stage, String name) throws Exception {
        String directory = System.getenv("ROSTER_UI_SCREENSHOTS");
        if (directory == null) {
            return;
        }
        Path folder = Path.of(directory);
        Files.createDirectories(folder);
        WritableImage snapshot = stage.getScene().snapshot(null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        ImageIO.write(image, "png", folder.resolve(name).toFile());
    }

    private static void close(WindowFixture fixture) throws Exception {
        onFxThread(() -> {
            fixture.stage().close();
            return null;
        });
    }

    private record WindowFixture(Stage stage, ModelManager model) {}
}
