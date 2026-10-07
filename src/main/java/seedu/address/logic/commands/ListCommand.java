package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Group;

/**
 * Lists all students or the students of one registered tutorial group.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists students with their attendance.\n"
            + "Usage: list [grp/GROUP]\nExample: list grp/T09";
    public static final String MESSAGE_SUCCESS = "Listed all %d students.";
    public static final String MESSAGE_EMPTY =
            "No students in Roster. Create a tutorial group and add students to get started.";
    public static final String MESSAGE_GROUP_SUCCESS = "Listed %d students in %s.";
    public static final String MESSAGE_GROUP_EMPTY = "Tutorial group %s has no students yet.";
    public static final String MESSAGE_GROUP_NOT_FOUND =
            "Tutorial group %1$s does not exist. Create it first with: init grp/%1$s";

    private final Group group;

    /**
     * Creates a command that lists students across all groups.
     */
    public ListCommand() {
        group = null;
    }

    /**
     * Creates a command that lists only students of {@code group}.
     */
    public ListCommand(Group group) {
        this.group = requireNonNull(group);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (group == null) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            int count = model.getFilteredPersonList().size();
            return new CommandResult(count == 0 ? MESSAGE_EMPTY : String.format(MESSAGE_SUCCESS, count));
        }
        if (!model.hasGroup(group)) {
            throw new CommandException(String.format(MESSAGE_GROUP_NOT_FOUND, group));
        }
        model.showGroup(group);
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? String.format(MESSAGE_GROUP_EMPTY, group)
                : String.format(MESSAGE_GROUP_SUCCESS, count, group));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand && Objects.equals(group, otherCommand.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group);
    }
}
