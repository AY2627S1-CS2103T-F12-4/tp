package seedu.address.ui;

import java.util.Objects;

import javafx.beans.property.ReadOnlyObjectProperty;
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
     * Creates a group panel whose selection follows the active tutorial group.
     */
    public GroupListPanel(ObservableList<Group> groupList, ReadOnlyObjectProperty<Group> activeGroup) {
        super(FXML);
        groupListView.setItems(groupList);
        selectGroup(activeGroup.get());
        activeGroup.addListener((observable, oldGroup, newGroup) -> selectGroup(newGroup));
        groupListView.getSelectionModel().selectedItemProperty().addListener((observable, oldGroup, newGroup) -> {
            if (!Objects.equals(newGroup, activeGroup.get())) {
                selectGroup(activeGroup.get());
            }
        });
    }

    private void selectGroup(Group group) {
        if (group == null) {
            groupListView.getSelectionModel().clearSelection();
        } else {
            groupListView.getSelectionModel().select(group);
        }
    }
}
