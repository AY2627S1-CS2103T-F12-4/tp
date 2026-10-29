package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.FxTestUtil.waitForRenderingCycles;
import static seedu.address.ui.MainWindowTestFixture.enterCommand;
import static seedu.address.ui.MainWindowTestFixture.getCommandInput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Group;
import seedu.address.testutil.PersonBuilder;

/** Checks the rendered group strip, including entries outside its horizontal viewport. */
public class GroupStripScrollUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void list_offscreenGroupAndAllStudents_revealsSelectionWithoutChangingData() throws Exception {
        try (MainWindowTestFixture fixture = createWindow()) {
            String savedBefore = Files.readString(fixture.storage().getAddressBookFilePath());
            AddressBook before = new AddressBook(fixture.model().getAddressBook());
            for (int width : List.of(740, 1100)) {
                onFxThread(() -> {
                    fixture.stage().setWidth(width);
                    return null;
                });
                runCommand(fixture.stage(), "list grp/T50");
                onFxThread(() -> {
                    assertSelectedAndVisible(fixture.stage(), "T50");
                    assertEquals(new Group("T50"), fixture.model().getActiveGroup().orElseThrow());
                    assertEquals(1, fixture.model().getFilteredPersonList().size());
                    return null;
                });
                runCommand(fixture.stage(), "list");
                onFxThread(() -> {
                    assertSelectedAndVisible(fixture.stage(), "All students");
                    assertEquals(before, fixture.model().getAddressBook());
                    return null;
                });
            }
            assertEquals(savedBefore, Files.readString(fixture.storage().getAddressBookFilePath()));
        }
    }

    @Test
    public void list_repeatedActiveView_revealsEntryAfterManualScrolling() throws Exception {
        try (MainWindowTestFixture fixture = createWindow()) {
            runCommand(fixture.stage(), "list grp/T50");
            scrollTo(fixture.stage(), 0);
            onFxThread(() -> {
                assertFalse(isFullyVisible(fixture.stage(), "T50"));
                return null;
            });
            runCommand(fixture.stage(), "list grp/T50");
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "T50");
                return null;
            });

            runCommand(fixture.stage(), "list");
            scrollTo(fixture.stage(), 50);
            onFxThread(() -> {
                assertFalse(isFullyVisible(fixture.stage(), "All students"));
                return null;
            });
            runCommand(fixture.stage(), "list");
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "All students");
                return null;
            });
        }
    }

    @Test
    public void activeGroup_alreadyVisible_preservesViewportAndCommandDraft() throws Exception {
        try (MainWindowTestFixture fixture = createWindow()) {
            scrollTo(fixture.stage(), 20);
            VisibleTarget target = onFxThread(() -> {
                List<ListCell<?>> cells = groupList(fixture.stage()).lookupAll(".list-cell").stream()
                        .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty())
                        .<ListCell<?>>map(node -> (ListCell<?>) node)
                        .filter(cell -> isFullyVisible(fixture.stage(), cell.getItem().toString()))
                        .sorted(Comparator.comparingInt(ListCell::getIndex)).toList();
                assertTrue(cells.size() >= 3, "The test needs several visible groups");
                ListCell<?> first = cells.getFirst();
                String selected = cells.get(cells.size() / 2).getItem().toString();
                return new VisibleTarget(selected, first.getItem().toString(), bounds(first).getMinX());
            });
            onFxThread(() -> {
                getCommandInput(fixture.stage()).setText("unfinished command");
                fixture.model().showGroup(new Group(target.group()));
                assertEquals("unfinished command", getCommandInput(fixture.stage()).getText());
                return null;
            });
            waitForRenderingCycles(3);
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), target.group());
                assertEquals(target.firstX(), bounds(cell(fixture.stage(), target.firstGroup())).getMinX(), 1,
                        "Selecting a visible group should not move the viewport");
                return null;
            });
        }
    }

    @Test
    public void registryChangesAndSearch_keepOnlyTheActiveViewRevealed() throws Exception {
        try (MainWindowTestFixture fixture = createWindow()) {
            runCommand(fixture.stage(), "init grp/T51");
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "T51");
                return null;
            });
            runCommand(fixture.stage(), "undo");
            onFxThread(() -> {
                assertFalse(fixture.model().hasGroup(new Group("T51")));
                assertSelectedAndVisible(fixture.stage(), "All students");
                return null;
            });
            runCommand(fixture.stage(), "redo");
            runCommand(fixture.stage(), "list grp/T51");
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "T51");
                return null;
            });
            runCommand(fixture.stage(), "find n/Nobody");
            onFxThread(() -> {
                assertNull(groupList(fixture.stage()).getSelectionModel().getSelectedItem());
                assertFalse(isFullyVisible(fixture.stage(), "All students"),
                        "A search must not scroll to an unrelated group");
                return null;
            });
            runCommand(fixture.stage(), "init grp/T00");
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "T00");
                return null;
            });
        }
    }

    @Test
    public void commands_beforeTheNextLayout_revealOnlyTheFinalView() throws Exception {
        try (MainWindowTestFixture fixture = createWindow()) {
            onFxThread(() -> {
                enterCommand(fixture.stage(), "list grp/T50");
                enterCommand(fixture.stage(), "find n/Nobody");
                return null;
            });
            waitForRenderingCycles(3);
            onFxThread(() -> {
                assertNull(groupList(fixture.stage()).getSelectionModel().getSelectedItem());
                assertTrue(isFullyVisible(fixture.stage(), "All students"),
                        "A cancelled scroll must not reveal the earlier group's entry");
                enterCommand(fixture.stage(), "list grp/T50");
                enterCommand(fixture.stage(), "list grp/T20");
                return null;
            });
            waitForRenderingCycles(3);
            onFxThread(() -> {
                assertSelectedAndVisible(fixture.stage(), "T20");
                return null;
            });
        }
    }

    private MainWindowTestFixture createWindow() throws Exception {
        AddressBook book = new AddressBook();
        for (int i = 1; i <= 50; i++) {
            book.addGroup(new Group(String.format(Locale.ROOT, "T%02d", i)));
        }
        book.addPerson(new PersonBuilder().withGroup("T50").build());
        MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder);
        waitForRenderingCycles(3);
        return fixture;
    }

    private static void runCommand(Stage stage, String command) throws Exception {
        onFxThread(() -> {
            enterCommand(stage, command);
            return null;
        });
        waitForRenderingCycles(3);
    }

    private static void scrollTo(Stage stage, int index) throws Exception {
        onFxThread(() -> {
            groupList(stage).scrollTo(index);
            return null;
        });
        waitForRenderingCycles(3);
    }

    private static ListView<?> groupList(Stage stage) {
        return (ListView<?>) stage.getScene().lookup("#groupListView");
    }

    private static ListCell<?> cell(Stage stage, String group) {
        return groupList(stage).lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> row && !row.isEmpty() && group.equals(row.getItem()))
                .map(node -> (ListCell<?>) node).findFirst().orElse(null);
    }

    private static Bounds bounds(Node node) {
        return node.localToScene(node.getLayoutBounds());
    }

    private static boolean isFullyVisible(Stage stage, String group) {
        ListCell<?> row = cell(stage, group);
        if (row == null || !row.isVisible()) {
            return false;
        }
        Bounds viewport = bounds(groupList(stage).lookup(".clipped-container"));
        Bounds entry = bounds(row);
        return entry.getMinX() >= viewport.getMinX() - 1 && entry.getMaxX() <= viewport.getMaxX() + 1;
    }

    private static void assertSelectedAndVisible(Stage stage, String group) {
        assertEquals(group, groupList(stage).getSelectionModel().getSelectedItem());
        assertTrue(isFullyVisible(stage, group), () -> group + " must be fully visible in the group strip: cell="
                + (cell(stage, group) == null ? "not rendered" : bounds(cell(stage, group)))
                + ", viewport=" + bounds(groupList(stage).lookup(".clipped-container")));
    }

    private record VisibleTarget(String group, String firstGroup, double firstX) {}
}
