package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final ObservableList<Session> sessionList;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} showing {@code personList}, with each person's attendance
     * for the sessions of their group in {@code sessionList}.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Session> sessionList) {
        super(FXML);
        this.sessionList = sessionList;
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // A new session adds an attendance cell to every student in its group, although no student changes
        sessionList.addListener((ListChangeListener<Session>) change -> personListView.refresh());
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
                setGraphic(new PersonCard(person, getIndex() + 1, sessionList).getRoot());
            }
        }
    }

}
