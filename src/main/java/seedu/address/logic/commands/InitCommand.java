package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;

/**
 * Registers a new tutorial group.
 */
public class InitCommand extends Command {

    public static final String COMMAND_WORD = "init";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Creates a tutorial group. "
            + "Parameters: " + PREFIX_GROUP + "GROUP\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_GROUP + "T09";

    public static final String MESSAGE_SUCCESS = "New tutorial group added: %1$s";
    public static final String MESSAGE_DUPLICATE_GROUP = "This tutorial group already exists: %1$s";

    private final Group group;

    /**
     * Creates an {@code InitCommand} for {@code group}.
     */
    public InitCommand(Group group) {
        this.group = requireNonNull(group);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasGroup(group)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_GROUP, group));
        }

        model.addGroup(group);
        model.setActiveGroup(group);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_NO_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, group));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof InitCommand otherCommand && group.equals(otherCommand.group));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("group", group).toString();
    }
}
