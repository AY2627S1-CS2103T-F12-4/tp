package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.StudentMatchesFiltersPredicate;

/**
 * Finds students matching every supplied filter, ordered by matriculation number.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds students matching all supplied filters.\n"
            + "Parameters: [n/KEYWORD] [m/MATRIC] [grp/GROUP] (at least one is required)\n"
            + "Example: " + COMMAND_WORD + " n/john tan grp/T09";

    public static final String MESSAGE_SUCCESS = "Found %1$d student(s) matching \"%2$s\".";
    public static final String MESSAGE_NO_RESULTS = "No students found matching \"%1$s\". Try another name.";
    public static final String MESSAGE_FILTER_SUCCESS = "Found %1$d student(s) matching filters: %2$s.";
    public static final String MESSAGE_FILTER_NO_RESULTS =
            "No students found matching filters: %1$s. Try another filter.";
    public static final String MESSAGE_UNKNOWN_PARAMETER =
            "Unexpected parameter. Use only n/KEYWORD, m/MATRIC or grp/GROUP.";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final StudentMatchesFiltersPredicate predicate;

    public FindCommand(NameContainsQueryPredicate predicate) {
        this(requireNonNull(predicate), null, null);
    }

    /**
     * Creates a search with optional filters; null means a filter was not supplied.
     */
    public FindCommand(NameContainsQueryPredicate namePredicate, Matric matric, Group group) {
        predicate = new StudentMatchesFiltersPredicate(namePredicate, matric, group);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (predicate.getGroup().isPresent() && !model.hasGroup(predicate.getGroup().get())) {
            logger.fine("Search rejected: tutorial group is not registered.");
            throw new CommandException(String.format(ListCommand.MESSAGE_GROUP_NOT_FOUND, predicate.getGroup().get()));
        }
        model.updateFilteredPersonList(predicate);
        assert model.getActiveGroup().isEmpty() : "A global search must clear the active tutorial group";
        int count = model.getFilteredPersonList().size();
        logger.fine("Student search completed with " + count + " matching students.");
        if (!predicate.isNameOnly()) {
            return new CommandResult(count == 0
                    ? String.format(MESSAGE_FILTER_NO_RESULTS, predicate.describeFilters())
                    : String.format(MESSAGE_FILTER_SUCCESS, count, predicate.describeFilters()));
        }
        String query = predicate.getNamePredicate().orElseThrow().getQuery();
        String message = count == 0
                ? String.format(MESSAGE_NO_RESULTS, query)
                : String.format(MESSAGE_SUCCESS, count, query);
        return new CommandResult(message);
    }

    @Override
    public boolean requiresSave() {
        return false;
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
