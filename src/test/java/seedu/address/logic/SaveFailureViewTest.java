package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.VersionedAddressBook;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Week;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Verifies that failed saves preserve the scope of a command and its retry. */
public class SaveFailureViewTest {
    private static final Group T09 = new Group("T09");
    private static final Group T10 = new Group("T10");

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private Logic logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("roster.json"));
        logic = createLogic(storage);
        for (String command : List.of("init grp/T09", "init grp/T10",
                "session grp/T09 w/1 d/2026-10-20", "session grp/T10 w/1 d/2026-10-21",
                "add n/Alice Tan e/alice@example.com m/A0000001X grp/T09",
                "add n/Anna Tan e/anna@example.com m/A0000002X grp/T09",
                "add n/Bob Tan e/bob@example.com m/A0000003X grp/T10",
                "list", "mark all w/1 s/present")) {
            logic.execute(command);
        }
    }

    @Test
    public void markAll_failedSave_keepsGroupAndRetryDoesNotMarkOtherGroups() throws Exception {
        for (IOException failure : List.of(new IOException("Simulated write failure"),
                new AccessDeniedException("roster.json"))) {
            logic.execute("list grp/T09");
            Person otherGroupStudent = model.getAddressBook().getPersonList().getLast();
            Logic failingLogic = createFailingOnceLogic(failure);
            assertFailurePreservesState(failingLogic, "mark all w/1 s/absent");

            failingLogic.execute("mark all w/1 s/absent");
            assertOnlyT09WasMarkedAbsent(otherGroupStudent);
            logic.execute("undo");
        }
    }

    @Test
    public void markAll_blockedDataFile_restoresGroupAndKeepsRetryScope() throws Exception {
        logic.execute("list grp/T09");
        Person otherGroupStudent = model.getAddressBook().getPersonList().getLast();
        Path target = storage.getAddressBookFilePath();
        Path backup = temporaryFolder.resolve("saved-roster.json");
        Files.move(target, backup);
        Files.createDirectory(target);
        Path blocker = Files.writeString(target.resolve("blocker"), "Prevent replacement of this directory");
        try {
            assertFailurePreservesState(logic, "mark all w/1 s/absent", backup);
        } finally {
            Files.delete(blocker);
            Files.delete(target);
            Files.move(backup, target);
        }

        logic.execute("mark all w/1 s/absent");
        assertOnlyT09WasMarkedAbsent(otherGroupStudent);
    }

    @Test
    public void deleteAll_failedSave_keepsSearchAndRetryDeletesOnlyItsResults() throws Exception {
        logic.execute("find n/Alice grp/T09 m/A0000001X");
        List<Person> survivors = List.copyOf(model.getAddressBook().getPersonList().subList(1, 3));
        Logic failingLogic = createFailingOnceLogic(new IOException("Simulated write failure"));
        assertFailurePreservesState(failingLogic, "delete all");

        failingLogic.execute("delete all");
        assertEquals(survivors, model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertFalse(model.showingAllStudentsProperty().get());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void clear_failedSave_restoresGroupRemovedByTheCommand() throws Exception {
        logic.execute("list grp/T10");
        assertFailurePreservesState(createFailingOnceLogic(new IOException("Simulated write failure")), "clear");
    }

    @Test
    public void init_failedSave_restoresEmptySearchInsteadOfShowingAllStudents() throws Exception {
        logic.execute("find n/Nobody");
        assertFailurePreservesState(createFailingOnceLogic(new IOException("Simulated write failure")), "init grp/T11");
    }

    @Test
    public void undoAndRedo_failedSave_restoreViewAndBothHistoryDirections() throws Exception {
        logic.execute("edit 1 n/Alice Lee");
        logic.execute("edit 2 n/Anna Lee");
        logic.execute("undo");
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        for (String view : List.of("list grp/T09", "find n/Alice")) {
            logic.execute(view);
            for (String command : List.of("undo", "redo")) {
                Logic failingLogic = createFailingOnceLogic(new IOException("Simulated write failure"));
                assertFailurePreservesState(failingLogic, command);
            }
        }
    }

    @Test
    public void clear_failedSave_restoresLivePredicateAndCustomOrder() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().fullName.startsWith("A"),
                Comparator.comparing((Person person) -> person.getMatric().value).reversed());
        assertFailurePreservesState(createFailingOnceLogic(new IOException("Simulated write failure")), "clear");

        Person alex = new PersonBuilder().withName("Alex Tan").withMatric("A0000004X")
                .withEmail("alex@example.com").withGroup("T09").build();
        model.addPerson(alex);
        assertEquals(List.of(alex, model.getAddressBook().getPersonList().get(1),
                model.getAddressBook().getPersonList().getFirst()), model.getFilteredPersonList());
    }

    @Test
    public void init_failedSave_restoresOriginalAllStudentsView() throws Exception {
        model = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        assertTrue(model.showingAllStudentsProperty().get());
        assertFailurePreservesState(createFailingOnceLogic(new IOException("Simulated write failure")), "init grp/T11");
    }

    private Logic createLogic(JsonAddressBookStorage rosterStorage) {
        return new LogicManager(model, new StorageManager(rosterStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private Logic createFailingOnceLogic(IOException failure) {
        return createLogic(new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            private boolean shouldFail = true;

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldFail) {
                    shouldFail = false;
                    throw failure;
                }
                super.saveAddressBook(addressBook);
            }
        });
    }

    private void assertFailurePreservesState(Logic failingLogic, String command) throws Exception {
        assertFailurePreservesState(failingLogic, command, storage.getAddressBookFilePath());
    }

    private void assertFailurePreservesState(Logic failingLogic, String command, Path savedFile) throws Exception {
        VersionedAddressBook before = new VersionedAddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        Optional<Group> groupBefore = model.getActiveGroup();
        boolean wasShowingAllStudents = model.showingAllStudentsProperty().get();
        String savedBefore = Files.readString(savedFile);

        assertThrows(CommandException.class, () -> failingLogic.execute(command));

        assertEquals(before, model.getAddressBook(), command);
        assertEquals(displayedBefore, model.getFilteredPersonList(), command);
        assertEquals(groupBefore, model.getActiveGroup(), command);
        assertEquals(wasShowingAllStudents, model.showingAllStudentsProperty().get(), command);
        VersionedAddressBook restored = (VersionedAddressBook) model.getAddressBook();
        assertEquals(before.getHistoryStates(), restored.getHistoryStates(), command);
        assertEquals(before.getHistoryCommands(), restored.getHistoryCommands(), command);
        assertEquals(before.getHistoryPointer(), restored.getHistoryPointer(), command);
        assertEquals(savedBefore, Files.readString(savedFile), command);
    }

    private void assertOnlyT09WasMarkedAbsent(Person otherGroupStudent) throws Exception {
        assertEquals(Optional.of(T09), model.getActiveGroup());
        assertEquals(2, model.getFilteredPersonList().size());
        for (Person student : model.getFilteredPersonList()) {
            assertEquals(Status.ABSENT, student.getAttendance(model.findSession(T09, new Week(1)).orElseThrow())
                    .orElseThrow().getStatus());
        }
        assertEquals(otherGroupStudent, model.getAddressBook().getPersonList().getLast());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
