package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

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
    private Label matric;
    @FXML
    private Label group;
    @FXML
    private FlowPane attendance;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex, List<Session> sessions) {
        super(FXML);
        requireAllNonNull(person, sessions);
        assert displayedIndex > 0 : "Displayed student indexes start from 1";
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        matric.setText(person.getMatric().value);
        group.setText(person.getGroup().value);
        for (AttendanceCell cell : AttendanceCell.forStudent(person, sessions)) {
            Label label = new Label("W" + cell.week() + ": " + cell.symbol());
            label.getStyleClass().addAll("attendance-cell", cell.styleClass());
            attendance.getChildren().add(label);
        }
        if (attendance.getChildren().isEmpty()) {
            attendance.getChildren().add(new Label("No sessions yet"));
        }
        configureAttendanceWrapping();
    }

    /**
     * Wraps attendance to the allotted card width, including before its first layout.
     */
    private void configureAttendanceWrapping() {
        double fixedColumnsWidth = cardPane.getChildren().stream().filter(node -> node != attendance)
                .mapToDouble(node -> node.prefWidth(-1)).sum();
        attendance.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(() ->
                Math.max(1, cardPane.getPrefWidth() - cardPane.getInsets().getLeft() - cardPane.getInsets().getRight()
                        - fixedColumnsWidth), cardPane.prefWidthProperty(), cardPane.insetsProperty()));
    }
}
