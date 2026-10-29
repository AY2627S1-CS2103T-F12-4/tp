package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;
import static seedu.address.ui.MainWindowTestFixture.enterCommand;
import static seedu.address.ui.MainWindowTestFixture.getCommandInput;

import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class UndoShortcutUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void shortcut_fromOutsideCommandBox_undoesRosterChange() throws Exception {
        Person original = new PersonBuilder().withName("Alex Tan").build();
        AddressBook book = new AddressBookBuilder().withPerson(original).build();
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                ModelManager model = fixture.model();
                enterCommand(stage, "edit 1 n/Alex Lim");
                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);

                TextArea result = getResultDisplay(stage);
                result.requestFocus();
                result.fireEvent(platformShortcutZ());

                assertEquals(original, model.getAddressBook().getPersonList().getFirst());
                assertEquals("Undid the most recent edit command.", result.getText());
                return null;
            });
        }
    }

    @Test
    public void shortcut_withUndoableCommandBoxText_doesNotUndoRosterChange() throws Exception {
        Person original = new PersonBuilder().withName("Alex Tan").build();
        AddressBook book = new AddressBookBuilder().withPerson(original).build();
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            onFxThread(() -> {
                Stage stage = fixture.stage();
                ModelManager model = fixture.model();
                enterCommand(stage, "edit 1 n/Alex Lim");
                TextField input = getCommandInput(stage);
                input.requestFocus();
                input.appendText("draft command");
                assertTrue(input.isFocused());
                assertTrue(input.isUndoable());
                input.fireEvent(platformShortcutZ());

                assertEquals("Alex Lim", model.getAddressBook().getPersonList().getFirst().getName().fullName);
                assertEquals("", input.getText());
                return null;
            });
        }
    }

    @Test
    public void shortcut_withoutHistory_showsErrorAndPreservesRoster() throws Exception {
        try (MainWindowTestFixture fixture = MainWindowTestFixture.create(new AddressBook(), temporaryFolder)) {
            onFxThread(() -> {
                TextArea result = getResultDisplay(fixture.stage());
                result.requestFocus();
                result.fireEvent(platformShortcutZ());
                assertEquals("There are no changes to undo.", result.getText());
                assertEquals(new AddressBook(), fixture.model().getAddressBook());
                return null;
            });
        }
    }

    private static TextArea getResultDisplay(Stage stage) {
        return (TextArea) stage.getScene().lookup("#resultDisplay");
    }

    private static KeyEvent platformShortcutZ() {
        boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.Z, false, !isMac, false, isMac);
    }
}
