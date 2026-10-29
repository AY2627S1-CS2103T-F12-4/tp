package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.PersonBuilder;

public class StudentDetailPanelTest {

    private static final Session WEEK_1 =
            new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
    private static final Session WEEK_2 =
            new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18"));

    @Test
    public void commentNotes_splitsTheJoinedHistoryIntoOneNotePerEntry() {
        assertEquals(List.of("attentive", "asks good questions"),
                StudentDetailPanel.commentNotes(new Comment("attentive; asks good questions")));
        assertEquals(List.of("attentive"), StudentDetailPanel.commentNotes(new Comment("attentive")));
        // #191: a separator the tutor types does the same job, which the User Guide documents
        assertEquals(List.of("Bring notes", "revise recursion"),
                StudentDetailPanel.commentNotes(new Comment("Bring notes; revise recursion")));
    }

    @Test
    public void commentNotes_emptyComment_hasNoNotes() {
        assertEquals(List.of(), StudentDetailPanel.commentNotes(Comment.EMPTY));
    }

    @Test
    public void commentNotes_semicolonWithoutASpace_staysOneNote() {
        // Only "; " joins notes, so a bare semicolon is part of the text the tutor typed
        assertEquals(List.of("see slides 3;4"),
                StudentDetailPanel.commentNotes(new Comment("see slides 3;4")));
    }

    @Test
    public void show_fillsEveryFieldAndStartsHidden() throws Exception {
        onFxThread(() -> {
            Person student = new PersonBuilder().withName("Priya Nair").withMatric("A0345678C")
                    .withGroup("T09").withEmail("priya@example.com").withTags("classrep")
                    .withComment("attentive; asks good questions")
                    .withAttendance(WEEK_1, Status.PRESENT)
                    .withAttendance(WEEK_2, Status.ABSENT)
                    .build();
            StudentDetailPanel panel = realised(new StudentDetailPanel(() -> { }));

            assertFalse(panel.isShowing());
            assertTrue(panel.shownStudent().isEmpty());

            panel.show(student, 3, List.of(WEEK_1, WEEK_2));

            assertTrue(panel.isShowing());
            assertEquals(student, panel.shownStudent().orElseThrow());
            assertEquals("Priya Nair", text(panel, "studentName"));
            assertEquals("A0345678C", text(panel, "studentMatric"));
            assertEquals("T09", text(panel, "studentGroup"));
            assertEquals("Row 3 in this list", text(panel, "rowNumber"));
            assertEquals("priya@example.com", text(panel, "detailEmail"));
            assertEquals("A0345678C", text(panel, "detailMatric"));
            assertEquals("T09", text(panel, "detailGroup"));
            assertEquals(List.of("classrep"), childTexts(panel, "detailTags"));
            // The bullet is a drawn dot beside the note, not part of its text
            assertEquals(List.of("attentive", "asks good questions"),
                    childTexts(panel, "commentBox"));
            assertEquals("1", text(panel, "presentCount"));
            assertEquals("1", text(panel, "absentCount"));
            assertEquals("0", text(panel, "unmarkedCount"));
            assertEquals("50% of marked sessions", text(panel, "attendanceRate"));
            assertEquals(0.5, ((ProgressBar) panel.getRoot().lookup("#attendanceBar")).getProgress(), 0.0001);
            assertEquals(2, ((Pane) panel.getRoot().lookup("#sessionRows")).getChildren().size());
            return null;
        });
    }

    @Test
    public void show_studentWithoutCommentOrTags_showsPlaceholders() throws Exception {
        onFxThread(() -> {
            Person student = new PersonBuilder().withGroup("T09").build();
            StudentDetailPanel panel = realised(new StudentDetailPanel(() -> { }));

            panel.show(student, 1, List.of());

            assertEquals(List.of("No comment yet"), childTexts(panel, "commentBox"));
            assertEquals(List.of("None"), childTexts(panel, "detailTags"));
            assertEquals("No sessions marked yet", text(panel, "attendanceRate"));
            assertEquals(List.of("This group has no sessions yet."), childTexts(panel, "sessionRows"));
            return null;
        });
    }

    @Test
    public void show_anotherStudent_swapsTheContents() throws Exception {
        onFxThread(() -> {
            Person first = new PersonBuilder().withName("Priya Nair").withMatric("A0345678C")
                    .withGroup("T09").withComment("attentive").build();
            Person second = new PersonBuilder().withName("Ben Lim").withMatric("A0456789D")
                    .withGroup("T09").build();
            StudentDetailPanel panel = realised(new StudentDetailPanel(() -> { }));

            panel.show(first, 3, List.of(WEEK_1));
            panel.show(second, 4, List.of(WEEK_1));

            assertEquals("Ben Lim", text(panel, "studentName"));
            assertEquals("Row 4 in this list", text(panel, "rowNumber"));
            assertEquals(List.of("No comment yet"), childTexts(panel, "commentBox"));
            assertEquals(second, panel.shownStudent().orElseThrow());
            return null;
        });
    }

    @Test
    public void show_sameStudentAndSessions_preservesScrollAndUpdatesRowNumber() throws Exception {
        onFxThread(() -> {
            Person student = new PersonBuilder().withMatric("A0345678C").withGroup("T09").build();
            StudentDetailPanel panel = realised(new StudentDetailPanel(() -> { }));
            panel.show(student, 3, List.of(WEEK_1));
            ScrollPane scroll = (ScrollPane) panel.getRoot().lookup("#detailScroll");
            scroll.setVvalue(0.75);

            panel.show(new PersonBuilder(student).build(), 1, List.of(WEEK_1));

            assertEquals(0.75, scroll.getVvalue(), 0.0001);
            assertEquals("Row 1 in this list", text(panel, "rowNumber"));

            panel.show(student, 1, List.of(WEEK_1, WEEK_2));
            assertEquals("2", text(panel, "unmarkedCount"));
            Person marked = new PersonBuilder(student).withAttendance(WEEK_1, Status.PRESENT).build();
            panel.show(marked, 1, List.of(WEEK_1, WEEK_2));
            assertEquals("1", text(panel, "presentCount"));
            assertEquals("1", text(panel, "unmarkedCount"));
            return null;
        });
    }

    @Test
    public void show_differentStudentOrReopenedPanel_startsAtTop() throws Exception {
        onFxThread(() -> {
            Person first = new PersonBuilder().withMatric("A0345678C").build();
            Person second = new PersonBuilder().withMatric("A0456789D").build();
            StudentDetailPanel panel = realised(new StudentDetailPanel(() -> { }));
            panel.show(first, 1, List.of(WEEK_1));
            ScrollPane scroll = (ScrollPane) panel.getRoot().lookup("#detailScroll");
            scroll.setVvalue(0.75);
            panel.show(second, 2, List.of(WEEK_1));
            assertEquals(0, scroll.getVvalue(), 0.0001);

            scroll.setVvalue(0.75);
            panel.hide();
            panel.show(second, 2, List.of(WEEK_1));
            assertEquals(0, scroll.getVvalue(), 0.0001);
            return null;
        });
    }

    @Test
    public void closeButton_hidesThePanelAndRunsTheCallback() throws Exception {
        onFxThread(() -> {
            AtomicInteger closes = new AtomicInteger();
            StudentDetailPanel panel = realised(new StudentDetailPanel(closes::incrementAndGet));
            panel.show(new PersonBuilder().build(), 1, List.of());

            Button close = (Button) panel.getRoot().lookup("#closeButton");
            close.fire();

            assertEquals(1, closes.get());
            // The panel closes through the callback, which the window routes back to hide()
            panel.hide();
            assertFalse(panel.isShowing());
            assertTrue(panel.shownStudent().isEmpty());
            return null;
        });
    }

    /**
     * Puts the panel in a scene and lays it out. A scroll pane only adds its contents to the scene
     * graph once it has a skin, so until then those nodes cannot be looked up by id.
     */
    private static StudentDetailPanel realised(StudentDetailPanel panel) {
        new Scene((Parent) panel.getRoot());
        panel.getRoot().applyCss();
        panel.getRoot().layout();
        return panel;
    }

    private static String text(StudentDetailPanel panel, String id) {
        return ((Label) panel.getRoot().lookup("#" + id)).getText();
    }

    /** Returns the text of each child, reading the first label inside a row when a row is nested. */
    private static List<String> childTexts(StudentDetailPanel panel, String id) {
        return ((Pane) panel.getRoot().lookup("#" + id)).getChildren().stream()
                .map(StudentDetailPanelTest::firstLabelText)
                .toList();
    }

    private static String firstLabelText(Node node) {
        if (node instanceof Label label) {
            return label.getText();
        }
        return ((Pane) node).getChildren().stream()
                .filter(child -> child instanceof Label)
                .map(child -> ((Label) child).getText())
                .findFirst().orElseThrow();
    }
}
