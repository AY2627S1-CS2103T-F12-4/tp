package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.MATRIC_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_commandsNotifyUiGroupAndSessionObservers() throws Exception {
        assertSame(model.showingAllStudentsProperty(), logic.showingAllStudentsProperty());
        assertTrue(logic.showingAllStudentsProperty().get());
        ReadOnlyObjectProperty<Group> activeGroup = logic.activeGroupProperty();
        ObservableList<Session> sessions = logic.getSessionList();
        List<Group> groupChanges = new ArrayList<>();
        List<Session> addedSessions = new ArrayList<>();
        activeGroup.addListener((observable, oldGroup, newGroup) -> groupChanges.add(newGroup));
        sessions.addListener((ListChangeListener<Session>) change -> {
            while (change.next()) {
                addedSessions.addAll(change.getAddedSubList());
            }
        });

        logic.execute("init grp/T09");
        assertEquals(new Group("T09"), activeGroup.get());
        logic.execute("session grp/T09 w/1 d/2026-08-11");
        Session expected = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
        assertEquals(List.of(expected), sessions);
        assertEquals(List.of(expected), addedSessions);
        logic.execute("list");
        assertNull(activeGroup.get());
        assertEquals(2, groupChanges.size());
        assertEquals(new Group("T09"), groupChanges.getFirst());
        assertNull(groupChanges.getLast());
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_EMPTY, model);
    }

    @Test
    public void execute_undo_restoresMultipleUndoableCommandsAndSavesState() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("add n/Alex Tan e/alex@example.com m/A0123456X grp/T09");
        logic.execute("edit 1 n/Alex Lim");

        assertEquals("Undid the most recent edit command.", logic.execute("undo").getFeedbackToUser());
        assertEquals("Alex Tan", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertEquals("Undid the most recent add command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("Undid the most recent init command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getGroupList().isEmpty());

        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(new AddressBook(), savedStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_undoAfterReadOnlyAndFailedCommands_restoresLastChange() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("list");
        assertCommandException("init grp/T09", "This tutorial group already exists: T09");

        assertEquals("Undid the most recent init command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getGroupList().isEmpty());
    }

    @Test
    public void execute_readOnlyCommands_doNotCopyRosterForHistory() throws Exception {
        CountingModelManager countingModel = new CountingModelManager();
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("readOnlyAddressBook.json"));
        Logic countingLogic = new LogicManager(countingModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("readOnlyUserPrefs.json"))));

        countingLogic.execute("list");
        countingLogic.execute("find n/Alex");
        countingLogic.execute("help");
        countingLogic.execute("exit");

        assertEquals(0, countingModel.addressBookReadCount);
    }

    @Test
    public void execute_delete_canBeUndoneWithoutClearingEarlierHistory() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("add n/Alex Tan e/alex@example.com m/A0123456X grp/T09");
        logic.execute("delete 1");

        assertEquals("Undid the most recent delete command.", logic.execute("undo").getFeedbackToUser());
        assertEquals("Alex Tan", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertEquals("Undid the most recent add command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.hasGroup(new Group("T09")));
    }

    @Test
    public void execute_clear_canBeUndoneWithoutClearingEarlierHistory() throws Exception {
        logic.execute("init grp/T09");
        logic.execute("clear");

        assertEquals("Undid the most recent clear command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.hasGroup(new Group("T09")));
        assertEquals("Undid the most recent init command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getGroupList().isEmpty());
    }

    @Test
    public void execute_undoWithNoHistory_throwsCommandException() {
        assertCommandException("undo", UndoCommand.MESSAGE_NOTHING_TO_UNDO);
    }

    @Test
    public void execute_invalidList_preservesFilteredListAndData() {
        model.addPerson(AMY);
        model.addPerson(BOB);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandFailure("list John", ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE), expectedModel);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void execute_undoSaveFails_restoresStateAndAllowsRetry() throws Exception {
        logic.execute("init grp/T09");
        AddressBook beforeUndo = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failed.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        Logic failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("failedPrefs.json"))));

        assertThrows(CommandException.class, () -> failingLogic.execute("undo"));
        assertEquals(beforeUndo, model.getAddressBook());
        assertTrue(model.canUndoAddressBook());
        assertEquals("Undid the most recent init command.", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getGroupList().isEmpty());
        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(new AddressBook(), savedStorage.readAddressBook().orElseThrow());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY
                + EMAIL_DESC_AMY + MATRIC_DESC_AMY + GROUP_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        // The add command rejects a student whose tutorial group is not registered.
        model.addGroup(expectedPerson.getGroup());
        ModelManager expectedModel = new ModelManager();
        expectedModel.addGroup(expectedPerson.getGroup());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private static class CountingModelManager extends ModelManager {
        private int addressBookReadCount;

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            addressBookReadCount++;
            return super.getAddressBook();
        }
    }
}
