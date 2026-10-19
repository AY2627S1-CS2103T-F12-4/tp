package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MATRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a student to Roster.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student to Roster. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_MATRIC + "MATRIC "
            + PREFIX_GROUP + "GROUP "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_MATRIC + "A0123456X "
            + PREFIX_GROUP + "T09 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";

    public static final String MESSAGE_SUCCESS = "Added %1$s (%2$s) to tutorial group %3$s.";
    public static final String MESSAGE_SUCCESS_SHOWING_GROUP =
            "Added %1$s (%2$s) to tutorial group %3$s. Now showing %3$s.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A student with matriculation number %1$s already exists.";
    public static final String MESSAGE_DUPLICATE_EMAIL = "A student with email %1$s already exists.";
    public static final String MESSAGE_UNKNOWN_GROUP =
            "Tutorial group %1$s does not exist. Create it first with the init command.";
    public static final String MESSAGE_UNKNOWN_PARAMETER =
            "Unexpected input. Use only: n/, e/, m/, grp/ and t/.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.hasGroup(toAdd.getGroup())) {
            throw new CommandException(String.format(MESSAGE_UNKNOWN_GROUP, toAdd.getGroup()));
        }

        if (model.hasPerson(toAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, toAdd.getMatric()));
        }

        if (model.getAddressBook().getPersonList().stream().anyMatch(toAdd::hasSameEmail)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_EMAIL, toAdd.getEmail()));
        }

        model.addPerson(toAdd);
        // A student outside the displayed list would be added out of sight, so their group is shown instead
        if (!model.getFilteredPersonList().contains(toAdd)) {
            model.showGroup(toAdd.getGroup());
            return new CommandResult(String.format(MESSAGE_SUCCESS_SHOWING_GROUP,
                    toAdd.getName(), toAdd.getMatric(), toAdd.getGroup()), toAdd);
        }
        // The list is sorted by matriculation number, so the new student can be anywhere in it
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                toAdd.getName(), toAdd.getMatric(), toAdd.getGroup()), toAdd);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
