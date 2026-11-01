package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Group;
import seedu.address.storage.CsvAttendanceExporter;
import seedu.address.storage.CsvAttendanceExporter.ExportSummary;
import seedu.address.storage.CsvFileAccess;

/**
 * Exports attendance for the full roster or one tutorial group to a new CSV file.
 */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";
    public static final String MESSAGE_USAGE = "export: Exports attendance to a new CSV file.\n"
            + "Parameters: f/FILE_PATH [grp/GROUP]\nExample: export f/attendance.csv grp/T09";
    public static final String MESSAGE_INVALID_PATH = "Provide a valid CSV file path ending in .csv.";
    public static final String MESSAGE_SUCCESS = "Exported %1$d student(s) in %2$d CSV row(s) to %3$s.";
    public static final String MESSAGE_FILE_EXISTS = "File already exists: %s. Choose another destination.";
    public static final String MESSAGE_WRITE_FAILURE = "Could not export attendance to %1$s: %2$s";

    private static final Logger logger = LogsCenter.getLogger(ExportCommand.class);

    private final Path destination;
    private final Optional<Group> group;
    private final CsvFileAccess files;

    /**
     * Creates an export command. A null group selects every student.
     */
    public ExportCommand(Path destination, Group group) {
        this(destination, group, new CsvFileAccess());
    }

    /** Creates an export using the supplied home-folder policy. */
    public ExportCommand(Path destination, Group group, CsvFileAccess files) {
        requireNonNull(destination);
        checkArgument(isValidDestination(destination), MESSAGE_INVALID_PATH);
        this.destination = destination.normalize();
        this.group = Optional.ofNullable(group);
        this.files = requireNonNull(files);
    }

    /**
     * Returns whether the destination has a CSV filename.
     */
    public static boolean isValidDestination(Path destination) {
        requireNonNull(destination);
        return destination.getFileName() != null
                && destination.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (group.isPresent() && !model.hasGroup(group.get())) {
            logger.fine("CSV export rejected: tutorial group is not registered.");
            throw new CommandException(String.format(ListCommand.MESSAGE_GROUP_NOT_FOUND, group.get()));
        }
        try {
            Path target = files.forWriting(destination);
            ExportSummary summary = new CsvAttendanceExporter().export(model.getAddressBook(), target, group);
            logger.fine("Attendance CSV export completed for " + summary.studentCount()
                    + " students in " + summary.rowCount() + " data rows.");
            return new CommandResult(String.format(MESSAGE_SUCCESS, summary.studentCount(), summary.rowCount(),
                    target));
        } catch (FileAlreadyExistsException e) {
            logger.fine("CSV export rejected: destination already exists.");
            throw new CommandException(String.format(MESSAGE_FILE_EXISTS, destination), e);
        } catch (IOException | SecurityException e) {
            logger.log(Level.WARNING, "Attendance CSV export failed.", e);
            throw new CommandException(String.format(MESSAGE_WRITE_FAILURE, destination, e.getMessage()), e);
        }
    }

    @Override
    public boolean requiresSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ExportCommand otherCommand
                && destination.equals(otherCommand.destination) && group.equals(otherCommand.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, group);
    }
}
