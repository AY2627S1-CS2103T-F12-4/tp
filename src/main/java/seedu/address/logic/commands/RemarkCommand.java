package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/**
 * Represents a request to change a person's remark.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates a person's remark.\n"
            + "Parameters: INDEX (positive integer) r/REMARK\n"
            + "Example: remark 1 r/Likes swimming";

    public static final String MESSAGE_ARGUMENTS = "Index: %1$d, Remark: %2$s";

    private final Index index;
    private final Remark remark;

    /**
     * Creates a command with the target index and requested remark.
     */
    public RemarkCommand(Index index, Remark remark) {
        requireAllNonNull(index, remark);
        this.index = index;
        this.remark = remark;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);

        List<Person> displayedPersons = model.getFilteredPersonList();
        int targetPosition = index.getZeroBased();

        if (targetPosition >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person original = displayedPersons.get(targetPosition);
        Person replacement = new Person(
                original.getName(),
                original.getPhone(),
                original.getEmail(),
                original.getAddress(),
                remark,
                original.getTags());

        model.setPerson(original, replacement);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        String feedback = remark.value.isEmpty()
                ? "Removed remark from: "
                : "Updated remark for: ";
        return new CommandResult(feedback + Messages.format(replacement));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RemarkCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index)
                && remark.equals(otherCommand.remark);
    }
}
