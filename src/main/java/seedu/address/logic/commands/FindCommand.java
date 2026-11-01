package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentMatchesFiltersPredicate;
import seedu.address.model.person.TopSimilarStudents;

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
    public static final String MESSAGE_DID_YOU_MEAN =
            "No students found matching \"%1$s\". Did you mean: %2$s?";
    public static final String MESSAGE_FILTER_DID_YOU_MEAN =
            "No students found matching filters: %1$s. Did you mean: %2$s?";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final StudentMatchesFiltersPredicate predicate;

    /**
     * Creates a search restricted to the supplied name query.
     */
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

        String suggestions = count == 0 ? suggestSimilarStudents(model) : null;

        if (!predicate.isNameOnly()) {
            if (count == 0) {
                return new CommandResult(suggestions == null
                        ? String.format(MESSAGE_FILTER_NO_RESULTS, describeFilters())
                        : String.format(MESSAGE_FILTER_DID_YOU_MEAN, describeFilters(), suggestions));
            }
            return new CommandResult(String.format(MESSAGE_FILTER_SUCCESS, count, describeFilters()));
        }

        String query = predicate.getNamePredicate().orElseThrow().getQuery();
        if (count == 0) {
            return new CommandResult(suggestions == null
                    ? String.format(MESSAGE_NO_RESULTS, query)
                    : String.format(MESSAGE_DID_YOU_MEAN, query, suggestions));
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, count, query));
    }

    /**
     * Describes the normalized filters in a consistent order for command feedback.
     */
    private String describeFilters() {
        List<String> filters = new ArrayList<>();
        predicate.getNamePredicate().ifPresent(name -> filters.add("n/\"" + name.getQuery() + "\""));
        predicate.getMatric().ifPresent(matric -> filters.add("m/" + matric.value));
        predicate.getGroup().ifPresent(group -> filters.add("grp/" + group.value));
        return String.join(" ", filters);
    }

    /**
     * Returns the students whose names come closest to the search query, or null when no name was
     * searched for, or when nothing is close enough to be worth offering.
     * <p>
     * Only called once the search has come back empty, so a search that finds someone never pays
     * for scoring names. Only students satisfying the other filters are offered, so a suggestion
     * never comes from outside the group that was searched.
     */
    private String suggestSimilarStudents(Model model) {
        if (predicate.getNamePredicate().isEmpty()) {
            return null;
        }
        TopSimilarStudents similar =
                new TopSimilarStudents(predicate.getNamePredicate().get().getQuery());
        // Nothing matched the name, so every student the other filters allow is a near miss.
        for (Person person : model.getAddressBook().getPersonList()) {
            if (predicate.matchesExceptName(person)) {
                similar.consider(person);
            }
        }
        return similar.isEmpty() ? null : describe(similar.best());
    }

    /** Names the suggested students with their matriculation numbers, which tell them apart. */
    private static String describe(List<Person> students) {
        return students.stream()
                .map(person -> person.getName().fullName + " (" + person.getMatric().value + ")")
                .collect(Collectors.joining(", "));
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
                .add("predicate", describeFilters())
                .toString();
    }
}
