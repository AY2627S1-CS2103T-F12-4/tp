package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
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

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label groupScope;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Session> sessions,
            ReadOnlyObjectProperty<Group> activeGroup) {
        super(FXML);
        this.sessions = sessions;
        groupScope.textProperty().bind(Bindings.createStringBinding(() -> activeGroup.get() == null
                ? "All groups" : "Tutorial group " + activeGroup.get(), activeGroup));
        personListView.setItems(personList);
        sessions.addListener((ListChangeListener<Session>) change -> personListView.refresh());
        personListView.setCellFactory(listView -> new PersonListViewCell());
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
        if (cell == null) {
            return false;
        }
        // The flow's own bounds are the part of the list that shows cards
        Bounds viewport = flow.get().localToScene(flow.get().getLayoutBounds());
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
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, sessions).getRoot());
            }
        }
    }

}
