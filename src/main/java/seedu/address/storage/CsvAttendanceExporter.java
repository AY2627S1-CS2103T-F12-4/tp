package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Group;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;

/**
 * Writes a complete attendance CSV before publishing it at a new destination.
 */
public class CsvAttendanceExporter {
    public static final String HEADER = "matric,name,email,group,week,date,attendance";

    /**
     * Exports all students or one group without changing the source roster or replacing an existing file.
     * The destination's parent directory must already exist.
     *
     * @throws IOException if the destination cannot be written.
     */
    public ExportSummary export(ReadOnlyAddressBook roster, Path destination, Optional<Group> group)
            throws IOException {
        requireNonNull(roster);
        requireNonNull(destination);
        requireNonNull(group);
        Path target = destination.toAbsolutePath().normalize();
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new FileAlreadyExistsException(target.toString());
        }
        List<Person> students = roster.getPersonList().stream()
                .filter(person -> group.isEmpty() || group.get().equals(person.getGroup()))
                .sorted(Comparator.comparing(person -> person.getMatric().value)).toList();
        Path temporary = Files.createTempFile(target.getParent(), ".roster-export-", ".tmp");
        try {
            int rows = writeCsv(temporary, students, roster.getSessionList());
            // Publish only the completed sibling file, without REPLACE_EXISTING.
            Files.move(temporary, target);
            return new ExportSummary(students.size(), rows);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private int writeCsv(Path file, List<Person> students, List<Session> sessions) throws IOException {
        int rows = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER + "\r\n");
            for (Person student : students) {
                List<Session> studentSessions = sessions.stream()
                        .filter(session -> session.getGroup().equals(student.getGroup()))
                        .sorted(Comparator.comparingInt(session -> session.getWeek().value)).toList();
                if (studentSessions.isEmpty()) {
                    writeRow(writer, student, "", "", "");
                    rows++;
                } else {
                    for (Session session : studentSessions) {
                        String attendance = student.getAttendance(session)
                                .map(record -> record.getStatus().name().toLowerCase(Locale.ROOT))
                                .orElse("unmarked");
                        writeRow(writer, student, String.valueOf(session.getWeek().value),
                                session.getDate().toString(), attendance);
                        rows++;
                    }
                }
            }
        }
        return rows;
    }

    private void writeRow(BufferedWriter writer, Person student, String week, String date, String attendance)
            throws IOException {
        List<String> fields = List.of(student.getMatric().value, student.getName().fullName,
                student.getEmail().value, student.getGroup().value, week, date, attendance);
        writer.write(String.join(",", fields.stream().map(CsvAttendanceExporter::escape).toList()) + "\r\n");
    }

    /**
     * Quotes CSV fields containing separators, quotes or line breaks.
     */
    static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Counts students and data rows, excluding the CSV header.
     */
    public record ExportSummary(int studentCount, int rowCount) {
    }
}
