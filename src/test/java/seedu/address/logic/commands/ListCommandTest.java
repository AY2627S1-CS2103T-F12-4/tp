package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, "Listed all 7 students.", expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, "Listed all 7 students.", expectedModel);
    }

    @Test
    public void execute_emptyRoster_showsEmptyMessage() {
        model = new ModelManager();
        expectedModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_EMPTY, expectedModel);
    }

    @Test
    public void execute_singleStudent_showsCount() {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), model, "Listed all 1 students.", expectedModel);
    }

    @Test
    public void execute_noSearchMatches_restoresAllStudents() {
        model.updateFilteredPersonList(person -> false);
        assertCommandSuccess(new ListCommand(), model, "Listed all 7 students.", expectedModel);
    }

    @Test
    public void equals_groupScopeDeterminesCommandIdentity() {
        ListCommand all = new ListCommand();
        ListCommand group = new ListCommand(new Group("T09"));
        ListCommand sameGroup = new ListCommand(new Group("T09"));
        assertEquals(all, all);
        assertEquals(all, new ListCommand());
        assertEquals(group, sameGroup);
        assertEquals(sameGroup, group);
        assertEquals(all.hashCode(), new ListCommand().hashCode());
        assertEquals(group.hashCode(), sameGroup.hashCode());
        assertFalse(all.equals(group));
        assertFalse(group.equals(all));
        assertFalse(group.equals(new ListCommand(new Group("T10"))));
        assertFalse(group.equals(null));
        assertFalse(group.equals("T09"));
    }
}
