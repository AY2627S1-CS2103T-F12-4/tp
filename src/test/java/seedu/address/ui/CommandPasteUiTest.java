package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class CommandPasteUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void paste_lineBreaks_preservesWordBoundaries() throws Exception {
        withClipboard(() -> {
            List<String> executed = new ArrayList<>();
            TextField input = inputFor(executed);
            for (String separator : List.of("\n", "\r\n", "\r", "\u0085", "\u2028", "\u2029", "\t")) {
                input.clear();
                paste(input, "find n/Beta" + separator + "Student");
                assertEquals("find n/Beta Student", input.getText());
                assertTrue(executed.isEmpty());
            }
            return null;
        });
    }

    @Test
    public void paste_selection_preservesSurroundingTextAndUndo() throws Exception {
        withClipboard(() -> {
            List<String> executed = new ArrayList<>();
            TextField input = inputFor(executed);
            String original = "find n/Alpha Wrong grp/T81";
            input.setText(original);
            input.selectRange("find n/".length(), "find n/Alpha Wrong".length());
            paste(input, "Beta\r\nStudent");
            assertEquals("find n/Beta Student grp/T81", input.getText());
            assertEquals("find n/Beta Student".length(), input.getCaretPosition());
            assertTrue(input.isUndoable());
            input.undo();
            assertEquals(original, input.getText());
            input.redo();
            assertEquals("find n/Beta Student grp/T81", input.getText());
            assertTrue(executed.isEmpty());
            return null;
        });
    }

    @Test
    public void paste_singleLine_preservesQuotedPathAndInternalSpaces() throws Exception {
        withClipboard(() -> {
            List<String> executed = new ArrayList<>();
            TextField input = inputFor(executed);
            String original = "export f/\"reports/A  B\u00a0C.csv\"";
            paste(input, original);
            assertEquals(original, input.getText());
            assertTrue(executed.isEmpty());
            return null;
        });
    }

    @Test
    public void paste_find_runsOnlyOnEnterAndMatchesSpacedName() throws Exception {
        withClipboard(() -> {
            ModelManager model = new ModelManager();
            JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
            LogicManager logic = new LogicManager(model,
                    new StorageManager(data, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            logic.execute("init grp/T81");
            logic.execute("add n/Beta Student e/beta@example.com m/A0888801A grp/T81");
            logic.execute("add n/Other Student e/other@example.com m/A0888802A grp/T81");
            byte[] before = Files.readAllBytes(data.getAddressBookFilePath());
            CommandBox box = new CommandBox(logic::execute, model.getGroupList());
            new Scene(box.getRoot());
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            paste(input, "find n/Beta\nStudent");
            assertEquals("find n/Beta Student", input.getText());
            assertEquals(2, model.getFilteredPersonList().size());
            input.fireEvent(new ActionEvent());
            assertEquals("", input.getText());
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals("Beta Student", model.getFilteredPersonList().getFirst().getName().fullName);
            assertEquals(2, model.getAddressBook().getPersonList().size());
            assertTrue(java.util.Arrays.equals(before, Files.readAllBytes(data.getAddressBookFilePath())));
            return null;
        });
    }

    private static TextField inputFor(List<String> executed) {
        CommandBox box = new CommandBox(command -> {
            executed.add(command);
            return new CommandResult("Done");
        }, FXCollections.observableArrayList());
        new Scene(box.getRoot());
        return (TextField) box.getRoot().lookup("#commandTextField");
    }

    private static void paste(TextField input, String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        assertTrue(Clipboard.getSystemClipboard().setContent(content));
        input.paste();
    }

    private static void withClipboard(Callable<Void> action) throws Exception {
        onFxThread(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Map<DataFormat, Object> previous = new HashMap<>();
            for (DataFormat format : clipboard.getContentTypes()) {
                previous.put(format, clipboard.getContent(format));
            }
            try {
                return action.call();
            } finally {
                clipboard.setContent(previous);
            }
        });
    }
}
