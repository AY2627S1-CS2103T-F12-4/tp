package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.person.Group;

public class CommandBoxTest {
    @Test
    public void completion_keyboardAndMouse_preservesInputAndExecutesOnlyOnEnter() throws Exception {
        List<String> executed = new ArrayList<>();
        ObservableList<Group> groups = FXCollections.observableArrayList(new Group("T09"), new Group("T10"));
        CountDownLatch focused = new CountDownLatch(1);
        Stage stage = onFxThread(() -> {
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Done");
            }, groups);
            Stage window = new Stage();
            window.setScene(new Scene(box.getRoot(), 500, 100));
            window.getScene().getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            TextField input = (TextField) window.getScene().lookup("#commandTextField");
            input.focusedProperty().addListener((observable, oldValue, value) -> {
                if (value) {
                    focused.countDown();
                }
            });
            window.show();
            window.requestFocus();
            window.getScene().lookup("#commandTextField").requestFocus();
            return window;
        });
        try {
            assertTrue(focused.await(10, TimeUnit.SECONDS), "Command input did not receive focus");
            onFxThread(() -> {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                assertTrue(input.isFocused());
                input.setText("ad");
                input.end();
                press(input, KeyCode.TAB);
                assertEquals("add", input.getText());
                assertTrue(executed.isEmpty());
                input.setText("list grp/T");
                input.end();
                Popup popup = popup(stage);
                ListView<?> options = (ListView<?>) popup.getContent().getFirst();
                assertEquals(2, options.getItems().size());
                press(input, KeyCode.DOWN);
                press(input, KeyCode.TAB);
                assertEquals("list grp/T10", input.getText());
                assertFalse(popup.isShowing());
                assertTrue(executed.isEmpty());

                input.setText("session grp/T99 w/3 d/2026-10-19");
                input.positionCaret("session grp/T".length());
                press(input, KeyCode.UP);
                press(input, KeyCode.TAB);
                assertEquals("session grp/T10 w/3 d/2026-10-19", input.getText());
                assertEquals("session grp/T10".length(), input.getCaretPosition());

                input.setText("list grp/T");
                input.end();
                press(input, KeyCode.ESCAPE);
                assertFalse(popup.isShowing());
                assertEquals("list grp/T", input.getText());
                input.appendText("1");
                groups.add(new Group("T11"));
                assertEquals(2, options.getItems().size());
                options.applyCss();
                options.layout();
                ListCell<?> cell = options.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                        .filter(row -> row.getIndex() == 1).findFirst().orElseThrow();
                assertEquals(Color.web("#f9fafb"), cell.getTextFill());
                assertEquals(Color.web("#293442"), cell.getBackground().getFills().getFirst().getFill());
                ListCell<?> highlighted = options.lookupAll(".list-cell").stream()
                        .map(node -> (ListCell<?>) node).filter(row -> row.getIndex() == 0).findFirst().orElseThrow();
                assertEquals(Color.WHITE, highlighted.getTextFill());
                assertEquals(Color.web("#2563eb"), highlighted.getBackground().getFills().getFirst().getFill());
                for (var type : List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
                        MouseEvent.MOUSE_CLICKED)) {
                    cell.fireEvent(new MouseEvent(type, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                            false, false, false, false, type == MouseEvent.MOUSE_PRESSED,
                            false, false, true, false, true, null));
                }
                assertEquals("list grp/T11", input.getText());
                assertTrue(executed.isEmpty());

                input.setText("list grp/T");
                input.end();
                press(input, KeyCode.ENTER);
                assertEquals(List.of("list grp/T"), executed);
                assertEquals("", input.getText());
                assertTrue(Window.getWindows().stream().noneMatch(window -> window instanceof Popup));
                return null;
            });
        } finally {
            onFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    private static Popup popup(Stage owner) {
        return (Popup) Window.getWindows().stream()
                .filter(window -> window instanceof Popup popup && popup.getOwnerWindow() == owner)
                .findFirst().orElseThrow();
    }

    private static void press(TextField input, KeyCode code) {
        input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }
}
