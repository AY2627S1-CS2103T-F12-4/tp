package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsQueryPredicate;

/**
 * Finds students whose names contain the search phrase, ordered by matriculation number.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds students whose names contain "
            + "the search phrase (case-insensitive) and displays them as a list with index numbers.\n"
            + "Results are ordered by matriculation number across all tutorial groups.\n"
            + "Parameters: n/KEYWORD\n"
            + "Example: " + COMMAND_WORD + " n/john tan";

    public static final String MESSAGE_SUCCESS = "Found %1$d student(s) matching \"%2$s\".";
    public static final String MESSAGE_NO_RESULTS = "No students found matching \"%1$s\". Try another name.";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unexpected parameter. Use only n/KEYWORD.";

    private final NameContainsQueryPredicate predicate;

    public FindCommand(NameContainsQueryPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int count = model.getFilteredPersonList().size();
        String message = count == 0
                ? String.format(MESSAGE_NO_RESULTS, predicate.getQuery())
                : String.format(MESSAGE_SUCCESS, count, predicate.getQuery());
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
