package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.ui.MainWindowTestFixture.enterCommand;
import static seedu.address.ui.MainWindowTestFixture.getCommandInput;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import seedu.address.model.AddressBook;

public class KeyboardFocusUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void tab_afterSuccessfulCommand_movesOutlineWithoutChangingFeedbackOrLayout() throws Exception {
        assertFocusIndicators("list", "success", "result-success");
    }

    @Test
    public void tab_afterFailedCommand_movesOutlineWithoutChangingFeedbackOrLayout() throws Exception {
        assertFocusIndicators("unknowncommand", "error", "result-error");
    }

    private void assertFocusIndicators(String command, String commandStyle, String resultStyle) throws Exception {
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(new AddressBook(), temporaryFolder)) {
            onFxThread(() -> {
                Parent root = fixture.stage().getScene().getRoot();
                TextField input = getCommandInput(fixture.stage());
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                Region commandBox = (Region) root.lookup("#commandBox");
                Region banner = (Region) root.lookup("#placeHolder");
                enterCommand(fixture.stage(), command);
                input.requestFocus();
                root.applyCss();
                root.layout();

                assertTrue(commandBox.getStyleClass().contains(commandStyle));
                assertTrue(banner.getStyleClass().contains(resultStyle));
                Border commandBorder = commandBox.getBorder();
                Border resultBorder = banner.getBorder();
                Background resultBackground = banner.getBackground();
                Text inputTextNode = (Text) input.lookup(".text");
                Text resultTextNode = (Text) result.lookup(".text");
                Paint inputTextColour = inputTextNode.getFill();
                Paint resultTextColour = resultTextNode.getFill();
                String feedback = result.getText();
                String inputText = input.getText();
                Bounds inputBounds = input.getLayoutBounds();
                Bounds resultBounds = result.getLayoutBounds();
                var inputInsets = input.getInsets();
                var resultInsets = result.getInsets();

                assertOutline(input, true);
                assertOutline(result, false);
                pressTab(input, false);
                root.applyCss();
                root.layout();
                assertOutline(input, false);
                assertOutline(result, true);

                assertEquals(inputBounds, input.getLayoutBounds());
                assertEquals(resultBounds, result.getLayoutBounds());
                assertEquals(inputInsets, input.getInsets());
                assertEquals(resultInsets, result.getInsets());
                assertEquals(commandBorder, commandBox.getBorder());
                assertEquals(resultBorder, banner.getBorder());
                assertEquals(resultBackground, banner.getBackground());
                assertEquals(inputTextColour, inputTextNode.getFill());
                assertEquals(resultTextColour, resultTextNode.getFill());

                pressTab(result, true);
                root.applyCss();
                root.layout();
                assertOutline(input, true);
                assertOutline(result, false);
                assertEquals(inputText, input.getText());
                assertEquals(feedback, result.getText());
                assertEquals(new AddressBook(), fixture.model().getAddressBook());
                return null;
            });
        }
    }

    private static void assertOutline(Region control, boolean isFocused) {
        assertEquals(isFocused, control.isFocused(), "Tab should move keyboard focus to the expected control");
        if (isFocused) {
            assertTrue(control.getBorder() != null && !control.getBorder().getStrokes().isEmpty(),
                    "The focused control should have a visible outline");
            var stroke = control.getBorder().getStrokes().getFirst();
            assertTrue(stroke.getTopStroke() instanceof Color colour && colour.getOpacity() > 0,
                    "The focused outline should not be transparent");
            assertTrue(stroke.getWidths().getTop() >= 2, "The focused outline should be clearly visible");
        } else if (control.getBorder() != null) {
            assertTrue(control.getBorder().getStrokes().stream()
                    .allMatch(stroke -> Color.TRANSPARENT.equals(stroke.getTopStroke())),
                    "The outline should disappear when the control loses focus");
        }
    }

    private static void pressTab(Region control, boolean isShiftDown) {
        control.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                isShiftDown, false, false, false));
    }
}
