package seedu.address.ui;

import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import seedu.address.model.person.Group;

/**
 * Navigates registered tutorial groups and the full roster using the existing list commands.
 */
public class GroupListPanel extends UiPart<Region> {
    private static final String FXML = "GroupListPanel.fxml";
    private static final String ALL_STUDENTS = "All students";

    private final ReadOnlyObjectProperty<Group> activeGroup;
    private final ReadOnlyBooleanProperty showingAllStudents;
    private final Consumer<String> commandExecutor;

    @FXML
    private ListView<String> groupListView;

    /**
     * Creates a group navigator whose selection follows the model's displayed view.
     */
    public GroupListPanel(ObservableList<Group> groups, ReadOnlyObjectProperty<Group> activeGroup,
            ReadOnlyBooleanProperty showingAllStudents, Consumer<String> commandExecutor) {
        super(FXML);
        this.activeGroup = activeGroup;
        this.showingAllStudents = showingAllStudents;
        this.commandExecutor = commandExecutor;
        groupListView.setItems(FXCollections.observableArrayList());
        refreshGroups(groups);
        groups.addListener((ListChangeListener<Group>) change -> refreshGroups(groups));
        activeGroup.addListener((observable, oldValue, newValue) -> selectActiveView());
        showingAllStudents.addListener((observable, oldValue, newValue) -> selectActiveView());
        groupListView.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                selectActiveView();
            }
        });
        groupListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                activateSelection();
                event.consume();
            }
        });
        groupListView.setOnMouseClicked(event -> {
            Node target = (Node) event.getTarget();
            while (target != null && !(target instanceof ListCell<?>)) {
                target = target.getParent();
            }
            if (event.getButton() == MouseButton.PRIMARY && target instanceof ListCell<?> cell && !cell.isEmpty()) {
                groupListView.getSelectionModel().select(cell.getIndex());
                activateSelection();
            }
        });
    }

    private void refreshGroups(ObservableList<Group> groups) {
        groupListView.getItems().setAll(ALL_STUDENTS);
        groupListView.getItems().addAll(groups.stream().map(Group::toString).sorted().toList());
        selectActiveView();
    }

    private void selectActiveView() {
        if (activeGroup.get() != null) {
            groupListView.getSelectionModel().select(activeGroup.get().toString());
        } else if (showingAllStudents.get()) {
            groupListView.getSelectionModel().selectFirst();
        } else {
            groupListView.getSelectionModel().clearSelection();
        }
    }

    private void activateSelection() {
        String selected = groupListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            commandExecutor.accept(selected.equals(ALL_STUDENTS) ? "list" : "list grp/" + selected);
            selectActiveView();
        }
    }
}
