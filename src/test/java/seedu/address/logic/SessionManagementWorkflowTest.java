package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.DeleteSessionCommand;
import seedu.address.logic.commands.EditSessionCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.CsvAttendanceExporter;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class SessionManagementWorkflowTest {
    private static final Group T09 = new Group("T09");
    private static final Group T10 = new Group("T10");
    private static final Session WEEK_ONE = new Session(T09, new Week(1), new SessionDate("2026-08-11"));
    private static final Session WEEK_TWO = new Session(T09, new Week(2), new SessionDate("2026-08-18"));
    private static final Session OTHER_GROUP = new Session(T10, new Week(1), new SessionDate("2026-08-12"));
    private static final Session RESCHEDULED = new Session(T09, new Week(1), new SessionDate("2026-08-13"));

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;
    private AddressBook initialState;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.addGroup(T09);
        book.addGroup(T10);
        book.addSession(WEEK_ONE);
        book.addSession(WEEK_TWO);
        book.addSession(OTHER_GROUP);
        book.addPerson(new PersonBuilder().withName("Sarah Tan").withMatric("A0000001X")
                .withEmail("sarah@example.com").withGroup("T09").withTags("classrep").withComment("Follow up")
                .withAttendance(WEEK_ONE, Status.PRESENT).withAttendance(WEEK_TWO, Status.ABSENT).build());
        book.addPerson(new PersonBuilder().withName("Ravi Kumar").withMatric("A0000002X")
                .withEmail("ravi@example.com").withGroup("T09").withAttendance(WEEK_ONE, Status.ABSENT).build());
        book.addPerson(new PersonBuilder().withName("Jamie Lee").withMatric("A0000003X")
                .withEmail("jamie@example.com").withGroup("T10").withAttendance(OTHER_GROUP, Status.PRESENT).build());
        book.addPerson(new PersonBuilder().withName("Unmarked Student").withMatric("A0000004X")
                .withEmail("unmarked@example.com").withGroup("T09").build());
        initialState = new AddressBook(book);
        model = new ModelManager(book, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        storage.saveAddressBook(book);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void changeStudentGroup_reportsClearedAttendanceAndUndoRestoresIt() throws Exception {
        logic.execute("find n/Sarah");
        String feedback = logic.execute("edit 1 grp/T10").getFeedbackToUser();
        assertTrue(feedback.endsWith("Moved from T09 to T10; cleared 2 attendance records. Use undo to restore."));
        assertTrue(student(0).getAttendances().isEmpty());
        assertEquals(T10, student(0).getGroup());
        assertEquals(initialState.getPersonList().get(1), student(1));
        assertSavedState();

        logic.execute("undo");
        assertEquals(initialState, model.getAddressBook());
        assertSavedState();
    }

    @Test
    public void edit_filteredView_updatesHiddenAttendanceAndRoundTripsToJsonAndCsv() throws Exception {
        logic.execute("find n/Sarah");
        assertEquals(String.format(EditSessionCommand.MESSAGE_SUCCESS, T09, new Week(1), WEEK_ONE.getDate(),
                RESCHEDULED.getDate()), logic.execute("editsession grp/T09 w/1 d/2026-08-13").getFeedbackToUser());

        assertEquals(List.of(student(0)), model.getFilteredPersonList());
        assertEquals(RESCHEDULED, model.findSession(T09, new Week(1)).orElseThrow());
        assertEquals(new Attendance(RESCHEDULED, Status.PRESENT), student(0).getAttendance(RESCHEDULED).orElseThrow());
        assertEquals(new Attendance(RESCHEDULED, Status.ABSENT), student(1).getAttendance(RESCHEDULED).orElseThrow());
        assertEquals(initialState.getPersonList().get(0).getAttendance(WEEK_TWO), student(0).getAttendance(WEEK_TWO));
        assertEquals(initialState.getPersonList().get(0).getTags(), student(0).getTags());
        assertEquals(initialState.getPersonList().get(0).getComment(), student(0).getComment());
        assertEquals(initialState.getPersonList().get(2), student(2));
        assertTrue(student(3).getAttendances().isEmpty());
        assertSavedState();
        Path csv = temporaryFolder.resolve("edited.csv");
        new CsvAttendanceExporter().export(model.getAddressBook(), csv, Optional.of(T09));
        assertTrue(Files.readString(csv).contains("T09,1,2026-08-13,present"));
        assertFalse(Files.readString(csv).contains("2026-08-11"));
    }

    @Test
    public void delete_otherGroupDisplayed_removesOnlyTargetAttendanceAndUndoRestoresEverything() throws Exception {
        logic.execute("list grp/T10");
        assertEquals(String.format(DeleteSessionCommand.MESSAGE_SUCCESS, T09, new Week(1), WEEK_ONE.getDate()),
                logic.execute("deletesession grp/T09 w/1").getFeedbackToUser());

        assertEquals(List.of(initialState.getPersonList().get(2)), model.getFilteredPersonList());
        assertEquals(Optional.of(T10), model.getActiveGroup());
        assertEquals(List.of(WEEK_TWO, OTHER_GROUP), model.getSessionList());
        assertTrue(student(0).getAttendance(WEEK_ONE).isEmpty());
        assertTrue(student(1).getAttendances().isEmpty());
        assertTrue(student(3).getAttendances().isEmpty());
        assertEquals(initialState.getPersonList().get(0).getAttendance(WEEK_TWO), student(0).getAttendance(WEEK_TWO));
        assertEquals(initialState.getPersonList().get(2), student(2));
        assertEquals(List.of(T09, T10), model.getGroupList());
        assertSavedState();
        Path csv = temporaryFolder.resolve("deleted.csv");
        new CsvAttendanceExporter().export(model.getAddressBook(), csv, Optional.of(T09));
        assertFalse(Files.readString(csv).contains("T09,1,"));

        assertEquals("Undid the most recent deletesession command.", logic.execute("undo").getFeedbackToUser());
        assertEquals(initialState, model.getAddressBook());
        assertSavedState();
    }

    @Test
    public void edit_thenUndo_restoresOriginalDateAndEmbeddedAttendance() throws Exception {
        logic.execute("editsession grp/T09 w/1 d/2026-08-13");
        assertEquals("Undid the most recent editsession command.", logic.execute("undo").getFeedbackToUser());
        assertEquals(initialState, model.getAddressBook());
        assertSavedState();
    }

    @Test
    public void delete_thenRecreate_doesNotResurrectAttendance() throws Exception {
        logic.execute("deletesession grp/T09 w/1");
        assertThrows(CommandException.class, () -> logic.execute("mark 1 w/1 s/present"));
        logic.execute("session grp/T09 w/1 d/2026-08-13");
        assertTrue(student(0).getAttendance(RESCHEDULED).isEmpty());
        assertTrue(student(1).getAttendance(RESCHEDULED).isEmpty());
        assertSavedState();
    }

    @Test
    public void invalidCommands_preserveDataViewAndUndoHistory() throws Exception {
        logic.execute("editsession grp/T09 w/1 d/2026-08-13");
        logic.execute("find n/Sarah");
        AddressBook beforeInvalidCommands = new AddressBook(model.getAddressBook());
        for (String input : List.of("editsession grp/T09 w/1 d/2026-02-29", "deletesession grp/T09 w/1 w/2",
                "deletesession grp/T09 w/1 d/2026-08-13", "editsession grp/T09 w/1 d/2026-08-13 x/value",
                "editsession grp/T09 w/1 d/2026-08-13 extra", "deletesession grp/T09 w/1 extra")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(beforeInvalidCommands, model.getAddressBook());
            assertEquals(List.of(student(0)), model.getFilteredPersonList());
            assertSavedState();
        }
        for (String input : List.of("editsession grp/T09 w/1 d/2026-08-13", "deletesession grp/T99 w/1",
                "editsession grp/T09 w/13 d/2026-08-13")) {
            assertThrows(CommandException.class, () -> logic.execute(input));
            assertEquals(beforeInvalidCommands, model.getAddressBook());
            assertEquals(List.of(student(0)), model.getFilteredPersonList());
            assertSavedState();
        }
        logic.execute("undo");
        assertEquals(initialState, model.getAddressBook());
    }

    @Test
    public void saveFailure_rollsBackEachOperationAndAllowsRetry() throws Exception {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("failure-preferences.json"))));
        for (String input : List.of("editsession grp/T09 w/1 d/2026-08-13", "deletesession grp/T09 w/1")) {
            assertThrows(CommandException.class, () -> failingLogic.execute(input));
            assertEquals(initialState, model.getAddressBook());
            assertFalse(model.canUndoAddressBook());
            assertSavedState();
            logic.execute(input);
            logic.execute("undo");
            assertEquals(initialState, model.getAddressBook());
        }
    }

    @Test
    public void emptyGroup_sessionsCanBeEditedAndDeleted() throws Exception {
        logic.execute("init grp/T11");
        logic.execute("session grp/T11 w/13 d/2026-11-03");
        logic.execute("find n/Nobody");
        logic.execute("editsession w/13 d/2026-11-04 grp/t11");
        logic.execute("deletesession w/13 grp/t11");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.hasGroup(new Group("T11")));
        assertTrue(model.findSession(new Group("T11"), new Week(13)).isEmpty());
        assertSavedState();
    }

    @Test
    public void delete_lastGroupSession_keepsStudentsAndExportsEmptySessionFields() throws Exception {
        logic.execute("deletesession grp/T10 w/1");
        assertTrue(model.hasGroup(T10));
        assertEquals(4, model.getAddressBook().getPersonList().size());
        assertTrue(student(2).getAttendances().isEmpty());
        Path csv = temporaryFolder.resolve("no-sessions.csv");
        new CsvAttendanceExporter().export(model.getAddressBook(), csv, Optional.of(T10));
        assertTrue(Files.readString(csv).contains("A0000003X,Jamie Lee,jamie@example.com,T10,,,"));
        assertSavedState();
    }

    private Person student(int index) {
        return model.getAddressBook().getPersonList().get(index);
    }

    private void assertSavedState() throws Exception {
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
