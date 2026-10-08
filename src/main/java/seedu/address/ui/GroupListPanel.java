package seedu.address.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Group;

/**
 * Panel containing the registered tutorial groups.
 */
public class GroupListPanel extends UiPart<Region> {

    private static final String FXML = "GroupListPanel.fxml";

    @FXML
    private ListView<Group> groupListView;

    /**
     * Creates a {@code GroupListPanel} backed by {@code groupList}.
     */
    public GroupListPanel(ObservableList<Group> groupList) {
        super(FXML);
        groupListView.setItems(groupList);
        groupList.addListener((ListChangeListener<Group>) change -> {
            if (!groupList.isEmpty()) {
                groupListView.getSelectionModel().selectLast();
            }
        });
    }
}
