package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.KEYWORD_ALL;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes every student in the displayed list, together with all their attendance records.
 * Tutorial groups and sessions are kept, even if they become empty.
 */
public class DeleteAllCommand extends Command {

    public static final String MESSAGE_SUCCESS =
            "Deleted %1$d student(s) in the displayed list, including all their attendance records.";
    public static final String MESSAGE_NO_STUDENTS = "There are no students in the displayed list to delete.";

    private static final Logger logger = LogsCenter.getLogger(DeleteAllCommand.class);

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> studentsToDelete = List.copyOf(model.getFilteredPersonList());
        if (studentsToDelete.isEmpty()) {
            throw new CommandException(MESSAGE_NO_STUDENTS);
        }

        // Attendance records are stored inside each student, so deleting a student deletes them too
        for (Person student : studentsToDelete) {
            model.deletePerson(student);
        }
        assert studentsToDelete.stream().noneMatch(model::hasPerson) : "No deleted student should remain in Roster";
        logger.fine("Deleted " + studentsToDelete.size() + " displayed student(s)");
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentsToDelete.size()));
    }

    /** Names the command as the user types it, rather than as one word. */
    @Override
    public String getUndoDescription() {
        return DeleteCommand.COMMAND_WORD + " " + KEYWORD_ALL;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteAllCommand;
    }

    @Override
    public String toString() {
        return DeleteAllCommand.class.getCanonicalName();
    }
}
