package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
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
    public void activeGroup_changesKeepSelectionInSync() throws Exception {
        onFxThread(() -> {
            Group first = new Group("T09");
            Group second = new Group("T10");
            ObservableList<Group> groups = FXCollections.observableArrayList(first, second);
            ReadOnlyObjectWrapper<Group> active = new ReadOnlyObjectWrapper<>(first);
            GroupListPanel panel = new GroupListPanel(groups, active.getReadOnlyProperty());
            ListView<?> list = groupList(panel);

            assertEquals(first, list.getSelectionModel().getSelectedItem());
            groups.add(new Group("T11"));
            assertEquals(first, list.getSelectionModel().getSelectedItem());
            active.set(second);
            assertEquals(second, list.getSelectionModel().getSelectedItem());
            active.set(null);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertEquals(-1, list.getSelectionModel().getSelectedIndex());
            list.getSelectionModel().select(0);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertEquals(-1, list.getSelectionModel().getSelectedIndex());
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
            GroupListPanel panel = new GroupListPanel(model.getGroupList(), model.activeGroupProperty());
            ListView<?> list = groupList(panel);

            parser.parseCommand("list grp/T09").execute(model);
            assertEquals(new Group("T09"), list.getSelectionModel().getSelectedItem());
            assertThrows(ParseException.class, () -> parser.parseCommand("find n/"));
            assertEquals(new Group("T09"), list.getSelectionModel().getSelectedItem());
            assertEquals(List.of(john), model.getFilteredPersonList());
            assertEquals(book, model.getAddressBook());

            parser.parseCommand("find n/john").execute(model);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertEquals(-1, list.getSelectionModel().getSelectedIndex());
            assertEquals(List.of(johnny, john), model.getFilteredPersonList());
            parser.parseCommand("list grp/T10").execute(model);
            assertEquals(new Group("T10"), list.getSelectionModel().getSelectedItem());
            parser.parseCommand("find n/Missing").execute(model);
            assertNull(list.getSelectionModel().getSelectedItem());
            assertEquals(-1, list.getSelectionModel().getSelectedIndex());
            assertEquals(List.of(), model.getFilteredPersonList());
            assertEquals(book, model.getAddressBook());
            return null;
        });
    }

    private static ListView<?> groupList(GroupListPanel panel) {
        return (ListView<?>) panel.getRoot().lookup("#groupListView");
    }
}
