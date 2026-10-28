package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /** Number of layouts to wait for a card to come fully into view, so a card that never fits cannot loop. */
    private static final int MAX_SCROLL_ATTEMPTS = 10;
    /** Pixels a card may extend past the visible area and still count as fully visible, for rounding. */
    private static final double VISIBILITY_TOLERANCE = 0.5;

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final ObservableList<Session> sessions;
    private Runnable pendingScroll;
    private BiConsumer<Person, Integer> onStudentChosen;

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label groupScope;
    @FXML
    private Label studentCount;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Session> sessions,
            ReadOnlyObjectProperty<Group> activeGroup) {
        super(FXML);
        requireAllNonNull(personList, sessions, activeGroup);
        this.sessions = sessions;
        groupScope.textProperty().bind(Bindings.createStringBinding(() -> activeGroup.get() == null
                ? "All groups" : "Tutorial group " + activeGroup.get(), activeGroup));
        studentCount.textProperty().bind(Bindings.createStringBinding(() -> personList.size()
                + (personList.size() == 1 ? " student" : " students"), personList));
        personListView.setItems(personList);
        sessions.addListener((ListChangeListener<Session>) change -> personListView.refresh());
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.setOnMouseClicked(this::openClickedStudent);
        // A filter, not a handler: a focused list consumes Enter itself to end its own editing.
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedStudent();
                event.consume();
            }
        });
    }

    /**
     * Sets what happens when a student's row is chosen, which is how their details are opened.
     * The callback receives the student and their row number in the displayed list.
     */
    public void setOnStudentChosen(BiConsumer<Person, Integer> onStudentChosen) {
        this.onStudentChosen = requireNonNull(onStudentChosen);
    }

    /**
     * Reports the clicked row, leaving the displayed list and its row numbers alone. Only the
     * highlight moves, which is the same highlight a command already uses to point at a student.
     */
    private void openClickedStudent(MouseEvent event) {
        if (onStudentChosen == null || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        Node target = (Node) event.getTarget();
        while (target != null && !(target instanceof ListCell<?>)) {
            target = target.getParent();
        }
        if (target instanceof ListCell<?> cell && !cell.isEmpty() && cell.getItem() instanceof Person person) {
            personListView.getSelectionModel().select(cell.getIndex());
            report(person, cell.getIndex() + 1);
        }
    }

    /**
     * Opens the highlighted student, which is how the keyboard reaches the details that the row no
     * longer shows. Enter means the same here as it does in the group navigator.
     */
    private void openSelectedStudent() {
        Person person = personListView.getSelectionModel().getSelectedItem();
        if (person != null) {
            report(person, personListView.getSelectionModel().getSelectedIndex() + 1);
        }
    }

    private void report(Person person, int displayedIndex) {
        if (onStudentChosen != null) {
            onStudentChosen.accept(person, displayedIndex);
        }
    }

    /**
     * Scrolls the list to {@code person} and highlights their card, if they are displayed.
     */
    public void scrollTo(Person person) {
        requireNonNull(person);
        int index = personListView.getItems().indexOf(person);
        if (index < 0) {
            // Commands only ask to show students they have displayed, so this points to a bug in a command
            logger.warning("Cannot scroll to " + person.getMatric() + ", who is not in the displayed list");
            return;
        }
        cancelPendingScroll();
        personListView.getSelectionModel().select(index);
        personListView.scrollTo(index);
        scrollIntoViewAfterLayout(person);
    }

    /**
     * Removes the highlight from any highlighted card.
     */
    public void clearSelection() {
        cancelPendingScroll();
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Keeps scrolling to {@code person}'s card after each layout of the list until the whole card is in view.
     * Cards differ in height, and the list only knows a card's real height once it has been laid out, so a
     * scroll made before then, such as right after the displayed list changes, can leave the card clipped.
     */
    private void scrollIntoViewAfterLayout(Person person) {
        Scene scene = personListView.getScene();
        if (scene == null) {
            return;
        }
        int[] attemptsLeft = {MAX_SCROLL_ATTEMPTS};
        pendingScroll = () -> {
            int index = personListView.getItems().indexOf(person);
            attemptsLeft[0]--;
            if (index < 0 || isFullyVisible(index) || attemptsLeft[0] < 0) {
                cancelPendingScroll();
                return;
            }
            getVirtualFlow().ifPresent(flow -> flow.scrollTo(index));
        };
        scene.addPostLayoutPulseListener(pendingScroll);
    }

    private void cancelPendingScroll() {
        if (pendingScroll != null && personListView.getScene() != null) {
            personListView.getScene().removePostLayoutPulseListener(pendingScroll);
        }
        pendingScroll = null;
    }

    /**
     * Returns true if the card at {@code index} is drawn and lies entirely within the visible part of the list.
     */
    private boolean isFullyVisible(int index) {
        Optional<VirtualFlow<?>> flow = getVirtualFlow();
        IndexedCell<?> cell = flow.map(virtualFlow -> virtualFlow.getVisibleCell(index)).orElse(null);
        Node visibleArea = personListView.lookup(".clipped-container");
        if (cell == null || visibleArea == null) {
            return false;
        }
        // Scrollbars occupy part of the flow, so only its clipped content area shows complete cards.
        Bounds viewport = visibleArea.localToScene(visibleArea.getBoundsInLocal());
        Bounds card = cell.localToScene(cell.getBoundsInLocal());
        return card.getMinY() >= viewport.getMinY() - VISIBILITY_TOLERANCE
                && card.getMaxY() <= viewport.getMaxY() + VISIBILITY_TOLERANCE;
    }

    private Optional<VirtualFlow<?>> getVirtualFlow() {
        Node flow = personListView.lookup(".virtual-flow");
        return flow instanceof VirtualFlow<?> virtualFlow ? Optional.of(virtualFlow) : Optional.empty();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        PersonListViewCell() {
            // Let the list's viewport determine row width, so attendance wraps instead of scrolling sideways.
            setPrefWidth(0);
            setWrapText(true);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                prefHeightProperty().unbind();
                setPrefHeight(USE_COMPUTED_SIZE);
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1, sessions).getRoot();
                card.setMinWidth(0);
                Node viewport = personListView.lookup(".clipped-container");
                card.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    double availableWidth = viewport == null ? getWidth() : viewport.getLayoutBounds().getWidth();
                    return Math.max(0, availableWidth - snappedLeftInset() - snappedRightInset());
                }, widthProperty(), insetsProperty(), viewport == null
                        ? personListView.widthProperty() : viewport.layoutBoundsProperty()));
                card.maxWidthProperty().bind(card.prefWidthProperty());
                configureCardContents(card);
                setGraphic(card);
                // Measure the styled content before the virtualized list caches this row's height.
                card.applyCss();
                // Notify the virtualized list when wrapping changes a row's preferred height.
                prefHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.ceil(Math.max(card.minHeight(card.getPrefWidth()), card.prefHeight(card.getPrefWidth()))
                                + snappedTopInset() + snappedBottomInset()),
                        card.prefWidthProperty(), card.needsLayoutProperty(), insetsProperty()));
            }
        }

        private void configureCardContents(Region card) {
            FlowPane attendance = (FlowPane) card.lookup("#attendance");
            HBox details = (HBox) attendance.getParent();
            double fixedColumnsWidth = details.getChildren().stream().filter(node -> node != attendance)
                    .mapToDouble(node -> node.prefWidth(-1)).sum();
            // Use the target column width even before the first layout, avoiding an oversized cached row.
            attendance.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(() ->
                    Math.max(1, card.getPrefWidth() - card.getInsets().getLeft() - card.getInsets().getRight()
                            - fixedColumnsWidth), card.prefWidthProperty(), card.insetsProperty()));
        }
    }

}
