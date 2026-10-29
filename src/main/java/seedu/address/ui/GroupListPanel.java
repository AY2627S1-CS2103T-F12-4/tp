package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ListViewSkin;
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
    private static final int MAX_SCROLL_ATTEMPTS = 10;
    private static final double VISIBILITY_TOLERANCE = 0.5;

    private final ReadOnlyObjectProperty<Group> activeGroup;
    private final ReadOnlyBooleanProperty showingAllStudents;
    private final Consumer<String> commandExecutor;
    private final GroupListSkin groupListSkin;
    private Scene pendingScrollScene;
    private Runnable pendingScroll;

    @FXML
    private ListView<String> groupListView;

    /**
     * Creates a group navigator whose selection follows the model's displayed view.
     */
    public GroupListPanel(ObservableList<Group> groups, ReadOnlyObjectProperty<Group> activeGroup,
            ReadOnlyBooleanProperty showingAllStudents, Consumer<String> commandExecutor) {
        super(FXML);
        requireAllNonNull(groups, activeGroup, showingAllStudents, commandExecutor);
        this.activeGroup = activeGroup;
        this.showingAllStudents = showingAllStudents;
        this.commandExecutor = commandExecutor;
        groupListView.setItems(FXCollections.observableArrayList());
        groupListSkin = new GroupListSkin(groupListView);
        groupListView.setSkin(groupListSkin);
        groupListView.sceneProperty().addListener((observable, oldValue, newValue) -> revealSelectionAfterLayout());
        refreshGroups(groups);
        groups.addListener((ListChangeListener<Group>) change -> refreshGroups(groups));
        activeGroup.addListener((observable, oldValue, newValue) -> refreshActiveView());
        showingAllStudents.addListener((observable, oldValue, newValue) -> refreshActiveView());
        groupListView.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                refreshActiveView();
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
        refreshActiveView();
    }

    /** Synchronizes and reveals the active view, including when a command lists the same view again. */
    void refreshActiveView() {
        if (activeGroup.get() != null) {
            groupListView.getSelectionModel().select(activeGroup.get().toString());
        } else if (showingAllStudents.get()) {
            groupListView.getSelectionModel().selectFirst();
        } else {
            groupListView.getSelectionModel().clearSelection();
        }
        revealSelectionAfterLayout();
    }

    private void activateSelection() {
        String selected = groupListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            commandExecutor.accept(selected.equals(ALL_STUDENTS) ? "list" : "list grp/" + selected);
            refreshActiveView();
        }
    }

    /** Waits until new entries and the viewport have their rendered sizes before scrolling. */
    private void revealSelectionAfterLayout() {
        cancelPendingScroll();
        Scene scene = groupListView.getScene();
        if (scene == null || groupListView.getSelectionModel().getSelectedIndex() < 0) {
            return;
        }
        pendingScrollScene = scene;
        int[] attemptsLeft = {MAX_SCROLL_ATTEMPTS};
        pendingScroll = () -> {
            if (groupListSkin.isSelectionVisible() || attemptsLeft[0]-- <= 0) {
                cancelPendingScroll();
                return;
            }
            groupListSkin.revealSelection();
            // A new or newly bold entry can need another layout before its final width is known.
            Platform.requestNextPulse();
        };
        scene.addPostLayoutPulseListener(pendingScroll);
        Platform.requestNextPulse();
    }

    private void cancelPendingScroll() {
        if (pendingScroll != null) {
            pendingScrollScene.removePostLayoutPulseListener(pendingScroll);
            pendingScroll = null;
            pendingScrollScene = null;
        }
    }

    /** Uses the list viewport to reveal an entry without moving an already visible selection. */
    private static class GroupListSkin extends ListViewSkin<String> {
        GroupListSkin(ListView<String> list) {
            super(list);
        }

        boolean isSelectionVisible() {
            int index = getSkinnable().getSelectionModel().getSelectedIndex();
            if (index < 0) {
                return true;
            }
            ListCell<?> cell = getVirtualFlow().getVisibleCell(index);
            Node viewport = getVirtualFlow().lookup(".clipped-container");
            if (cell == null || viewport == null) {
                return false;
            }
            Bounds entry = viewport.sceneToLocal(cell.localToScene(cell.getLayoutBounds()));
            Bounds visible = viewport.getLayoutBounds();
            return entry.getMinX() >= visible.getMinX() - VISIBILITY_TOLERANCE
                    && entry.getMaxX() <= visible.getMaxX() + VISIBILITY_TOLERANCE;
        }

        void revealSelection() {
            int index = getSkinnable().getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                getVirtualFlow().scrollTo(index);
            }
        }
    }
}
