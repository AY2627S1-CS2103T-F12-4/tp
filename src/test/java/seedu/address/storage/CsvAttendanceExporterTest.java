package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.model.AddressBook;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.storage.CsvAttendanceExporter.ExportSummary;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class CsvAttendanceExporterTest {
    @TempDir
    public Path temporaryFolder;

    private final CsvAttendanceExporter exporter = new CsvAttendanceExporter();
    private final Session weekOne = new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11"));
    private final Session weekTwo = new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18"));
    private final Person marked = new PersonBuilder().withName("Élodie Tan").withMatric("A0000002X")
            .withEmail("elodie@example.com").withAttendance(weekOne, Status.PRESENT)
            .withAttendance(weekTwo, Status.ABSENT).build();
    private final Person unmarked = new PersonBuilder().withName("New Student").withMatric("A0000001X")
            .withEmail("new@example.com").build();
    private final Person noSessions = new PersonBuilder().withName("Other Group").withMatric("A0000003X")
            .withEmail("other@example.com").withGroup("T10").build();

    @Test
    public void exportSummary_incompleteCounts_detectsInternalCountingErrors() {
        assertThrows(AssertionError.class, () -> new CsvAttendanceExporter.ExportSummary(-1, 0));
        assertThrows(AssertionError.class, () -> new CsvAttendanceExporter.ExportSummary(2, 1));
        assertEquals(0, new CsvAttendanceExporter.ExportSummary(0, 0).rowCount());
        assertEquals(3, new CsvAttendanceExporter.ExportSummary(2, 3).rowCount());
    }

    @Test
    public void export_allGroups_ordersStudentsAndWeeksAndPreservesAttendanceStates() throws Exception {
        AddressBook roster = roster();
        AddressBook original = new AddressBook(roster);
        Path output = temporaryFolder.resolve("all attendance.csv");
        assertEquals(new ExportSummary(3, 5), exporter.export(roster, output, Optional.empty()));
        assertEquals(CsvAttendanceExporter.HEADER + "\r\n"
                + "A0000001X,New Student,new@example.com,T09,1,2026-08-11,unmarked\r\n"
                + "A0000001X,New Student,new@example.com,T09,2,2026-08-18,unmarked\r\n"
                + "A0000002X,Élodie Tan,elodie@example.com,T09,1,2026-08-11,present\r\n"
                + "A0000002X,Élodie Tan,elodie@example.com,T09,2,2026-08-18,absent\r\n"
                + "A0000003X,Other Group,other@example.com,T10,,,\r\n",
                Files.readString(output, StandardCharsets.UTF_8));
        assertEquals(original, roster);
        assertEquals(List.of("all attendance.csv"), filenames());
    }

    @Test
    public void export_singleGroup_usesOnlyItsStudentsAndSessions() throws Exception {
        Path output = temporaryFolder.resolve("T10.csv");
        assertEquals(new ExportSummary(1, 1), exporter.export(roster(), output, Optional.of(new Group("T10"))));
        assertEquals(List.of(CsvAttendanceExporter.HEADER,
                "A0000003X,Other Group,other@example.com,T10,,,"), Files.readAllLines(output));
    }

    @Test
    public void export_emptyRosterAndEmptyGroup_writeHeaderOnly() throws Exception {
        AddressBook empty = new AddressBook();
        empty.addGroup(new Group("T09"));
        for (Optional<Group> group : List.of(Optional.<Group>empty(), Optional.of(new Group("T09")))) {
            Path output = temporaryFolder.resolve(group.isEmpty() ? "all.csv" : "group.csv");
            assertEquals(new ExportSummary(0, 0), exporter.export(empty, output, group));
            assertEquals(CsvAttendanceExporter.HEADER + "\r\n", Files.readString(output));
        }
    }

    @Test
    public void export_existingDestination_preservesFileAndRemovesTemporaryOutput() throws Exception {
        Path output = temporaryFolder.resolve("existing.csv");
        Files.writeString(output, "keep this content");
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(roster(), output, Optional.empty()));
        assertEquals("keep this content", Files.readString(output));
        assertEquals(List.of("existing.csv"), filenames());
    }

    @Test
    public void export_invalidParent_leavesNoPartialExportOrExtraDirectories() {
        Path output = temporaryFolder.resolve("missing/report.csv");
        assertThrows(IOException.class, () -> exporter.export(roster(), output, Optional.empty()));
        assertFalse(Files.exists(output));
        assertFalse(Files.exists(output.getParent()));
    }

    @Test
    public void export_destinationAppearsDuringWrite_preservesItAndRemovesTemporaryOutput() throws Exception {
        Path output = temporaryFolder.resolve("concurrent.csv");
        AddressBook roster = beforeReadingSessions(() -> {
            assertTrue(Files.exists(temporaryOutput()));
            Files.writeString(output, "created by another writer");
        });

        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(roster, output, Optional.empty()));

        assertEquals("created by another writer", Files.readString(output));
        assertEquals(List.of("concurrent.csv"), filenames());
        assertEquals(roster(), roster);
    }

    @Test
    public void export_writeAndCleanupFail_preservesWriteFailureAndSuppressesCleanupFailure() throws Exception {
        Path output = temporaryFolder.resolve("failed.csv");
        AddressBook roster = beforeReadingSessions(() -> {
            Path temporary = temporaryOutput();
            Files.delete(temporary);
            Files.createDirectory(temporary);
            Files.writeString(temporary.resolve("block-cleanup"), "keep");
        });

        IOException failure = assertThrows(IOException.class, () -> exporter.export(roster, output, Optional.empty()));

        assertFalse(failure instanceof DirectoryNotEmptyException, "The write error must remain the primary failure");
        assertEquals(1, failure.getSuppressed().length);
        assertTrue(failure.getSuppressed()[0] instanceof DirectoryNotEmptyException);
        assertFalse(Files.exists(output));
        assertEquals(roster(), roster);
    }

    @Test
    public void escape_separatorsQuotesAndLineBreaks_producesValidCsvFields() {
        assertEquals("plain", CsvAttendanceExporter.escape("plain"));
        assertEquals("", CsvAttendanceExporter.escape(""));
        assertEquals("\"Tan, Sarah\"", CsvAttendanceExporter.escape("Tan, Sarah"));
        assertEquals("\"a\"\"b\"", CsvAttendanceExporter.escape("a\"b"));
        assertEquals("\"first\r\nsecond\"", CsvAttendanceExporter.escape("first\r\nsecond"));
        assertEquals("\"first\rsecond\"", CsvAttendanceExporter.escape("first\rsecond"));
        assertEquals("Élodie 李", CsvAttendanceExporter.escape("Élodie 李"));
    }

    private AddressBook roster() {
        AddressBook roster = new AddressBookBuilder().withPerson(noSessions).withPerson(marked)
                .withPerson(unmarked).build();
        roster.addSession(weekTwo);
        roster.addSession(weekOne);
        return roster;
    }

    /** Changes only the test folder after export creates a temporary file and before writing starts. */
    private AddressBook beforeReadingSessions(IoAction action) {
        return new AddressBook(roster()) {
            @Override
            public ObservableList<Session> getSessionList() {
                try {
                    action.run();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                return super.getSessionList();
            }
        };
    }

    private Path temporaryOutput() throws IOException {
        try (var paths = Files.list(temporaryFolder)) {
            return paths.filter(path -> path.getFileName().toString().startsWith(".roster-export-"))
                    .findFirst().orElseThrow();
        }
    }

    private List<String> filenames() throws IOException {
        try (var paths = Files.list(temporaryFolder)) {
            return paths.map(path -> path.getFileName().toString()).sorted().toList();
        }
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }
}
