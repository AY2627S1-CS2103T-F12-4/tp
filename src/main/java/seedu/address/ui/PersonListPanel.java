package seedu.address.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final ObservableList<Session> sessions;

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
        int index = personListView.getItems().indexOf(person);
        if (index < 0) {
            return;
        }
        personListView.getSelectionModel().select(index);
        personListView.scrollTo(index);
        // Cards differ in height, so the first scroll uses estimated heights and can stop short of the card.
        // Scrolling again once the cards have been laid out brings the whole card into view.
        Platform.runLater(() -> personListView.scrollTo(index));
    }

    /**
     * Removes the highlight from any highlighted card.
     */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
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
