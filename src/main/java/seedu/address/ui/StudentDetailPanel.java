package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.tag.Tag;

/**
 * Shows one student in full beside the list: their details, their comment as a history of notes,
 * and their attendance for every session of their group.
 * <p>
 * The panel only reads. Opening, closing and switching students leave the displayed list, its row
 * numbers, the active group and the stored data exactly as they were.
 */
public class StudentDetailPanel extends UiPart<Region> {

    private static final String FXML = "StudentDetailPanel.fxml";
    private static final String NO_COMMENT = "No comment yet";
    private static final String NO_SESSIONS = "This group has no sessions yet.";
    private static final String NOTHING_MARKED = "No sessions marked yet";
    /** Dates read as "Tue 18 Aug 2026" here: the year is shown so a mistyped date can be seen. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM uuuu", Locale.ENGLISH);

    /** The student on show, or null while the panel is hidden. */
    private Person shown;
    private List<Session> shownSessions = List.of();
    private Runnable pendingScrollRestore;

    @FXML
    private Label avatar;
    @FXML
    private Button closeButton;
    @FXML
    private ScrollPane detailScroll;
    @FXML
    private Label studentName;
    @FXML
    private Label studentMatric;
    @FXML
    private Label studentGroup;
    @FXML
    private Label rowNumber;
    @FXML
    private Label detailEmail;
    @FXML
    private Label detailMatric;
    @FXML
    private Label detailGroup;
    @FXML
    private FlowPane detailTags;
    @FXML
    private VBox commentBox;
    @FXML
    private Label presentCount;
    @FXML
    private Label absentCount;
    @FXML
    private Label unmarkedCount;
    @FXML
    private ProgressBar attendanceBar;
    @FXML
    private Label attendanceRate;
    @FXML
    private VBox sessionRows;

    /**
     * Creates a panel that is hidden until a student is shown in it.
     *
     * @param onClose run when the panel's own close button is pressed.
     */
    public StudentDetailPanel(Runnable onClose) {
        super(FXML);
        requireNonNull(onClose);
        closeButton.setOnAction(event -> onClose.run());
        hide();
    }

    /**
     * Fills the panel with {@code person} and shows it, replacing whoever was shown before.
     *
     * @param displayedIndex the student's row number in the list currently on screen.
     */
    public void show(Person person, int displayedIndex, List<Session> sessions) {
        requireAllNonNull(person, sessions);
        rowNumber.setText("Row " + displayedIndex + " in this list");
        // Read-only commands may change the row number without changing the panel's contents.
        if (person.equals(shown) && sessions.equals(shownSessions)) {
            return;
        }
        // A command on the open student rebuilds these contents. Keeping the scroll leaves the
        // reader where they were, rather than sending them back to the top of a long panel.
        boolean sameStudent = shown != null && shown.getMatric().equals(person.getMatric());
        double keptScroll = sameStudent ? scrolledPixels() : 0;
        shown = person;
        shownSessions = List.copyOf(sessions);
        avatar.setText(initialsOf(person.getName().fullName));
        studentName.setText(person.getName().fullName);
        studentMatric.setText(person.getMatric().value);
        studentGroup.setText(person.getGroup().value);
        detailEmail.setText(person.getEmail().value);
        detailMatric.setText(person.getMatric().value);
        detailGroup.setText(person.getGroup().value);

        fillTags(person);
        fillComment(person.getComment());
        fillAttendance(person, sessions);

        getRoot().setVisible(true);
        getRoot().setManaged(true);
        cancelPendingScrollRestore();
        if (!sameStudent) {
            detailScroll.setVvalue(0);
            return;
        }
        // Applied twice on purpose: now, from the heights the last layout measured, and again once
        // the rebuilt contents have been laid out and their real height is known.
        applyScrolledPixels(keptScroll);
        restoreScrollAfterLayout(keptScroll);
    }

    /**
     * Scrolls the panel to {@code pixels}, clamped, since the contents can have become shorter,
     * as when a comment is cleared.
     */
    private void applyScrolledPixels(double pixels) {
        double scrollable = scrollableHeight();
        detailScroll.setVvalue(scrollable <= 0 ? 0 : Math.min(pixels, scrollable) / scrollable);
    }

    /**
     * Returns how far the panel is scrolled, in pixels rather than as the proportion
     * {@code vvalue} holds. A new note or a newly marked session makes the contents taller, and the
     * same proportion of a taller panel is a different place, so a proportion would drift.
     */
    private double scrolledPixels() {
        double scrollable = scrollableHeight();
        return scrollable <= 0 ? 0 : detailScroll.getVvalue() * scrollable;
    }

    /** Returns the height that can be scrolled through, which is zero while everything fits. */
    private double scrollableHeight() {
        if (detailScroll.getContent() == null) {
            return 0;
        }
        return detailScroll.getContent().getLayoutBounds().getHeight()
                - detailScroll.getViewportBounds().getHeight();
    }

    /**
     * Corrects the scroll once the rebuilt contents have been laid out, because a new note or a
     * newly marked session changes their height and that is not known until then.
     */
    private void restoreScrollAfterLayout(double pixels) {
        Scene scene = detailScroll.getScene();
        if (scene == null) {
            return;
        }
        pendingScrollRestore = () -> {
            cancelPendingScrollRestore();
            applyScrolledPixels(pixels);
        };
        scene.addPostLayoutPulseListener(pendingScrollRestore);
        Platform.requestNextPulse();
    }

    private void cancelPendingScrollRestore() {
        if (pendingScrollRestore != null && detailScroll.getScene() != null) {
            detailScroll.getScene().removePostLayoutPulseListener(pendingScrollRestore);
        }
        pendingScrollRestore = null;
    }

    /** Hides the panel, leaving the list beneath it untouched. */
    public void hide() {
        cancelPendingScrollRestore();
        shown = null;
        shownSessions = List.of();
        getRoot().setVisible(false);
        getRoot().setManaged(false);
    }

    public boolean isShowing() {
        return getRoot().isVisible();
    }

    /** Returns the student on show, or empty while the panel is hidden. */
    public Optional<Person> shownStudent() {
        return Optional.ofNullable(shown);
    }

    /**
     * Splits a comment into the notes it was built from.
     * {@code c/} joins notes with {@link Comment#SEPARATOR}, so a stored comment is a short history
     * and is worth showing as one bullet per note. An empty comment has no notes.
     */
    static List<String> commentNotes(Comment comment) {
        requireNonNull(comment);
        if (comment.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(comment.value.split(Pattern.quote(Comment.SEPARATOR)))
                .map(String::strip)
                .filter(note -> !note.isEmpty())
                .toList();
    }

    private void fillTags(Person person) {
        detailTags.getChildren().clear();
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> detailTags.getChildren().add(tagLabel(tag)));
        if (detailTags.getChildren().isEmpty()) {
            detailTags.getChildren().add(subtleLabel("None"));
        }
    }

    private void fillComment(Comment comment) {
        commentBox.getChildren().clear();
        List<String> notes = commentNotes(comment);
        if (notes.isEmpty()) {
            commentBox.getChildren().add(subtleLabel(NO_COMMENT));
            return;
        }
        for (String note : notes) {
            commentBox.getChildren().add(noteRow(note));
        }
    }

    /** Builds one note as a bullet beside its text, so a long note wraps under itself. */
    private static HBox noteRow(String note) {
        Region bullet = new Region();
        bullet.getStyleClass().add("detail-bullet");

        Label text = new Label(note);
        text.getStyleClass().add("detail-note");
        text.setWrapText(true);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(10, bullet, text);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    /**
     * Returns the initials shown in the panel's avatar: the first letter of the first and last
     * parts of a name, which is what distinguishes two students at a glance.
     */
    static String initialsOf(String fullName) {
        requireNonNull(fullName);
        String[] parts = fullName.strip().split("\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "";
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (parts.length == 1 ? first : first + last).toUpperCase(Locale.ROOT);
    }

    private void fillAttendance(Person person, List<Session> sessions) {
        AttendanceSummary summary = AttendanceSummary.of(person, sessions);
        presentCount.setText(Integer.toString(summary.present()));
        absentCount.setText(Integer.toString(summary.absent()));
        unmarkedCount.setText(Integer.toString(summary.unmarked()));
        attendanceBar.setProgress(summary.attendanceRate());
        attendanceRate.setText(summary.marked() == 0
                ? NOTHING_MARKED
                : Math.round(summary.attendanceRate() * 100) + "% of marked sessions");

        sessionRows.getChildren().clear();
        List<Session> ownSessions = sessions.stream()
                .filter(session -> session.getGroup().equals(person.getGroup()))
                .sorted(Comparator.comparingInt(session -> session.getWeek().value))
                .toList();
        if (ownSessions.isEmpty()) {
            sessionRows.getChildren().add(subtleLabel(NO_SESSIONS));
            return;
        }
        for (Session session : ownSessions) {
            sessionRows.getChildren().add(sessionRow(person, session));
        }
    }

    /** Builds one "Week 1 | Tue 18 Aug | Present" row. */
    private HBox sessionRow(Person person, Session session) {
        Label week = new Label("Week " + session.getWeek().value);
        week.getStyleClass().add("detail-session-week");

        Label date = new Label(DATE_FORMAT.format(session.getDate().value));
        date.getStyleClass().add("detail-session-date");
        HBox.setHgrow(date, Priority.ALWAYS);
        date.setMaxWidth(Double.MAX_VALUE);

        Status status = person.getAttendance(session).map(Attendance::getStatus).orElse(null);
        Label state = new Label(statusGlyph(status) + " " + statusText(status));
        state.getStyleClass().addAll("status-pill", statusStyle(status));

        HBox row = new HBox(week, date, state);
        row.getStyleClass().add("detail-session-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String statusGlyph(Status status) {
        if (status == Status.PRESENT) {
            return "✓";
        }
        return status == Status.ABSENT ? "✗" : "—";
    }

    private static String statusText(Status status) {
        if (status == Status.PRESENT) {
            return "Present";
        }
        return status == Status.ABSENT ? "Absent" : "Unmarked";
    }

    private static String statusStyle(Status status) {
        if (status == Status.PRESENT) {
            return "status-present";
        }
        return status == Status.ABSENT ? "status-absent" : "status-unmarked";
    }

    private static Label tagLabel(Tag tag) {
        Label label = new Label(tag.tagName);
        label.getStyleClass().add("detail-tag");
        return label;
    }

    private static Label subtleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-muted");
        return label;
    }
}
