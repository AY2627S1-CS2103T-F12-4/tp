package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class GroupListPanelTest {
    @Test
    public void activeView_commandsAndRegistryChanges_keepSelectionInSync() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addGroup(new Group("T10"));
            model.addGroup(new Group("T09"));
            GroupListPanel panel = new GroupListPanel(model.getGroupList(), model.activeGroupProperty(),
                    model.showingAllStudentsProperty(), command -> {});
            ListView<?> list = groupList(panel);
            AddressBookParser parser = new AddressBookParser();

            assertEquals(List.of("All students", "T09", "T10"), list.getItems());
            assertEquals("All students", list.getSelectionModel().getSelectedItem());
            parser.parseCommand("list grp/T09").execute(model);
            assertEquals("T09", list.getSelectionModel().getSelectedItem());
            parser.parseCommand("list").execute(model);
            assertEquals("All students", list.getSelectionModel().getSelectedItem());
            parser.parseCommand("find n/Missing").execute(model);
            assertNull(list.getSelectionModel().getSelectedItem());
            parser.parseCommand("list").execute(model);
            assertEquals("All students", list.getSelectionModel().getSelectedItem());
            parser.parseCommand("init grp/T08").execute(model);
            assertEquals(List.of("All students", "T08", "T09", "T10"), list.getItems());
            assertEquals("T08", list.getSelectionModel().getSelectedItem());
            model.setAddressBook(new AddressBook());
            assertEquals(List.of("All students"), list.getItems());
            assertEquals("All students", list.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    @Test
    public void activation_enterRunsListAndEmptySelectionDoesNothing() throws Exception {
        onFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addGroup(new Group("T09"));
            List<String> commands = new ArrayList<>();
            GroupListPanel panel = new GroupListPanel(model.getGroupList(), model.activeGroupProperty(),
                    model.showingAllStudentsProperty(), commands::add);
            ListView<?> list = groupList(panel);
            list.getSelectionModel().select(1);
            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
            assertTrue(commands.isEmpty());
            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
            assertEquals(List.of("list grp/T09"), commands);
            // The callback did not change the model, so selection returns to the actual view.
            assertEquals("All students", list.getSelectionModel().getSelectedItem());
            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
            assertEquals(List.of("list grp/T09", "list"), commands);
            list.getSelectionModel().clearSelection();
            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
            assertEquals(2, commands.size());
            return null;
        });
    }

    @Test
    public void find_validAndInvalidInput_updatesOrPreservesGroupSelection() throws Exception {
        onFxThread(() -> {
            Person john = new PersonBuilder().withName("John Tan").withMatric("A0000002X")
                    .withGroup("T09").build();
            Person johnny = new PersonBuilder().withName("Johnny Lim").withMatric("A0000001X")
                    .withGroup("T10").build();
            AddressBook book = new AddressBookBuilder().withPerson(john).withPerson(johnny).build();
            ModelManager model = new ModelManager(book, new UserPrefs());
            AddressBookParser parser = new AddressBookParser();
            GroupListPanel panel = new GroupListPanel(model.getGroupList(), model.activeGroupProperty(),
                    model.showingAllStudentsProperty(), command -> {});
            ListView<?> list = groupList(panel);

            parser.parseCommand("list grp/T09").execute(model);
            assertEquals("T09", list.getSelectionModel().getSelectedItem());
            assertThrows(ParseException.class, () -> parser.parseCommand("find n/"));
            assertEquals("T09", list.getSelectionModel().getSelectedItem());
            assertEquals(List.of(john), model.getFilteredPersonList());
            assertEquals(book, model.getAddressBook());

            parser.parseCommand("find n/john").execute(model);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertEquals(List.of(johnny, john), model.getFilteredPersonList());
            parser.parseCommand("list grp/T10").execute(model);
            assertEquals("T10", list.getSelectionModel().getSelectedItem());
            parser.parseCommand("find n/Missing").execute(model);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(book, model.getAddressBook());
            return null;
        });
    }

    private static ListView<?> groupList(GroupListPanel panel) {
        return (ListView<?>) panel.getRoot().lookup("#groupListView");
    }
}
