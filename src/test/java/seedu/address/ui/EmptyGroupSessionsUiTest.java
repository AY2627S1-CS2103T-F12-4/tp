package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.testutil.FxTestUtil.waitForRenderingCycles;
import static seedu.address.ui.MainWindowTestFixture.enterCommand;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class EmptyGroupSessionsUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void emptyGroup_commandsRefreshSummaryAndListingPreservesData() throws Exception {
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(new AddressBook(), temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                enterCommand(stage, "init grp/T11");
                enterCommand(stage, "session grp/T11 w/2 d/2026-10-27");
                enterCommand(stage, "session grp/T11 w/1 d/2026-10-20");
                enterCommand(stage, "init grp/T12");
                AddressBook beforeListing = new AddressBook(fixture.model().getAddressBook());
                String savedBefore = Files.readString(fixture.storage().getAddressBookFilePath());
                enterCommand(stage, "list grp/T11");
                assertEquals("Sessions in T11\nWeek 1: 2026-10-20\nWeek 2: 2026-10-27", summary(stage).getText());
                assertEquals(beforeListing, fixture.model().getAddressBook());
                assertEquals(savedBefore, Files.readString(fixture.storage().getAddressBookFilePath()));

                enterCommand(stage, "editsession grp/T11 w/1 d/2026-10-21");
                assertTrue(summary(stage).getText().contains("Week 1: 2026-10-21"));
                enterCommand(stage, "deletesession grp/T11 w/2");
                assertEquals("Sessions in T11\nWeek 1: 2026-10-21", summary(stage).getText());
                enterCommand(stage, "undo");
                enterCommand(stage, "list grp/T11");
                assertTrue(summary(stage).getText().contains("Week 2: 2026-10-27"));
                enterCommand(stage, "redo");
                enterCommand(stage, "list grp/T11");
                assertEquals("Sessions in T11\nWeek 1: 2026-10-21", summary(stage).getText());

                enterCommand(stage, "list grp/T12");
                assertTrue(summary(stage).getText().startsWith("No sessions in T12 yet."));
                enterCommand(stage, "list");
                assertEquals("No students to display.", summary(stage).getText());
                enterCommand(stage, "list grp/T11");
                enterCommand(stage, "deletesession grp/T11 w/1");
                assertTrue(summary(stage).getText().startsWith("No sessions in T11 yet."));
                enterCommand(stage, "session grp/T11 w/3 d/2026-11-03");
                assertEquals("Sessions in T11\nWeek 3: 2026-11-03", summary(stage).getText());
                enterCommand(stage, "add n/Alice Tan e/alice@example.com m/A0000001X grp/T11");
                assertEquals(1, fixture.model().getFilteredPersonList().size());
                enterCommand(stage, "delete 1");
                enterCommand(stage, "list grp/T11");
                assertEquals("Sessions in T11\nWeek 3: 2026-11-03", summary(stage).getText());
                return null;
            });
        }
    }

    @Test
    public void emptyGroup_allWeeksCanBeScrolledAtMinimumAndLargerWindowSizes() throws Exception {
        AddressBook book = new AddressBook();
        Group group = new Group("T11");
        book.addGroup(group);
        for (int week = 13; week >= 1; week--) {
            book.addSession(new Session(group, new Week(week),
                    new SessionDate(LocalDate.of(2026, 8, 11).plusWeeks(week - 1).toString())));
        }
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            for (int[] size : List.of(new int[]{740, 600}, new int[]{1100, 800})) {
                onFxThread(() -> {
                    fixture.stage().setWidth(size[0]);
                    fixture.stage().setHeight(size[1]);
                    enterCommand(fixture.stage(), "list grp/T11");
                    return null;
                });
                waitForRenderingCycles(3);
                onFxThread(() -> {
                    Label label = summary(fixture.stage());
                    assertEquals(14, label.getText().lines().count());
                    assertTrue(label.getText().endsWith("Week 13: 2026-11-03"));
                    ScrollPane scroll = (ScrollPane) fixture.stage().getScene().lookup("#emptyGroupSummary");
                    scroll.setVvalue(1);
                    return null;
                });
                waitForRenderingCycles(3);
                onFxThread(() -> {
                    Stage stage = fixture.stage();
                    ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#emptyGroupSummary");
                    var viewport = scroll.lookup(".viewport");
                    Bounds visible = viewport.localToScene(viewport.getBoundsInLocal());
                    Bounds content = summary(stage).localToScene(summary(stage).getBoundsInLocal());
                    assertTrue(visible.getHeight() > 20, "The session summary needs a usable scroll area");
                    assertTrue(content.getWidth() <= visible.getWidth() + 1);
                    assertTrue(content.getMaxY() <= visible.getMaxY() + 1, "The final week must be reachable");
                    assertTrue(content.getMaxY() > visible.getMinY(), "The final week must be visible");
                    saveScreenshot(stage, "empty-group-" + size[0] + "x" + size[1] + ".png");
                    return null;
                });
            }
        }
    }

    private static Label summary(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        Label label = (Label) stage.getScene().lookup("#emptyGroupSessions");
        assertNotNull(label, "An empty group must expose its sessions without a student");
        return label;
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
}
