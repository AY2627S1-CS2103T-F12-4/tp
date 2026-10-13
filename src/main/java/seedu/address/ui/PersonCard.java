package seedu.address.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    static final String TICK = "✓";
    static final String CROSS = "✗";
    static final String DASH = "—";
    static final String STYLE_PRESENT = "attendance_present";
    static final String STYLE_ABSENT = "attendance_absent";
    static final String STYLE_UNMARKED = "attendance_unmarked";

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private FlowPane attendances;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display, and the person's
     * attendance for each session of their group in {@code sessions}.
     */
    public PersonCard(Person person, int displayedIndex, List<Session> sessions) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        for (Session session : getSessionsOfGroup(person, sessions)) {
            Label cell = new Label(getAttendanceText(person, session));
            cell.getStyleClass().add(getAttendanceStyleClass(person, session));
            attendances.getChildren().add(cell);
        }
    }

    /**
     * Returns the sessions of {@code person}'s group in {@code sessions}, sorted by week.
     * The cells come from the group's sessions rather than the person's records, so that a session the
     * person has not been marked for still has a cell.
     */
    static List<Session> getSessionsOfGroup(Person person, List<Session> sessions) {
        return sessions.stream()
                .filter(session -> session.getGroup().equals(person.getGroup()))
                .sorted(Comparator.comparing(Session::getWeek))
                .toList();
    }

    /**
     * Returns the text of {@code person}'s attendance cell for {@code session}: the week, followed by a tick
     * if the person was present, a cross if absent, or a dash if they have not been marked yet.
     */
    static String getAttendanceText(Person person, Session session) {
        String mark = getStatus(person, session)
                .map(status -> status == Status.PRESENT ? TICK : CROSS)
                .orElse(DASH);
        return "W" + session.getWeek() + " " + mark;
    }

    /**
     * Returns the style class that colors {@code person}'s attendance cell for {@code session}.
     */
    static String getAttendanceStyleClass(Person person, Session session) {
        return getStatus(person, session)
                .map(status -> status == Status.PRESENT ? STYLE_PRESENT : STYLE_ABSENT)
                .orElse(STYLE_UNMARKED);
    }

    private static Optional<Status> getStatus(Person person, Session session) {
        return person.getAttendance(session).map(Attendance::getStatus);
    }
}
