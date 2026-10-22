package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMENT_REPLACE;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Person;

/**
 * Adds to, replaces, or clears the comment on a student identified by their displayed index.
 */
public class CommentCommand extends Command {

    public static final String COMMAND_WORD = "comment";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds to or replaces a student's comment.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_COMMENT + "COMMENT to add to the existing comment, or "
            + PREFIX_COMMENT_REPLACE + "COMMENT to replace it "
            + "(" + PREFIX_COMMENT_REPLACE + " on its own clears it)\n"
            + "Example: " + COMMAND_WORD + " 3 " + PREFIX_COMMENT + "asks good questions";

    public static final String MESSAGE_APPEND_SUCCESS = "Updated comment for %1$s: %2$s";
    public static final String MESSAGE_REPLACE_SUCCESS = "Replaced comment for %1$s: %2$s";
    public static final String MESSAGE_CLEAR_SUCCESS = "Cleared the comment for %1$s.";
    public static final String MESSAGE_ALREADY_EMPTY = "%1$s has no comment to clear.";

    private static final Logger logger = LogsCenter.getLogger(CommentCommand.class);

    private final Index index;
    private final Comment comment;
    private final boolean isReplacing;

    /**
     * Creates a command that updates the comment of the student at {@code index}.
     *
     * @param comment the text to add or to replace with; {@link Comment#EMPTY} clears the comment,
     *                which is only meaningful when {@code isReplacing} is true.
     * @param isReplacing true to overwrite the existing comment, false to append to it.
     */
    public CommentCommand(Index index, Comment comment, boolean isReplacing) {
        requireAllNonNull(index, comment);
        this.index = index;
        this.comment = comment;
        this.isReplacing = isReplacing;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToComment = lastShownList.get(index.getZeroBased());
        String name = personToComment.getName().fullName;

        if (isClearing() && personToComment.getComment().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_EMPTY, name));
        }

        Comment updatedComment = updatedCommentFor(personToComment);

        // The displayed list is left as it is, so that the indexes the TA sees do not change.
        model.setPerson(personToComment, personToComment.withComment(updatedComment));
        logger.fine((isClearing() ? "Cleared the comment of " : "Updated the comment of ")
                + personToComment.getMatric());

        return new CommandResult(successMessage(name, updatedComment));
    }

    /**
     * Returns the comment that replaces {@code person}'s current one.
     *
     * @throws CommandException if appending would take the comment over its length limit.
     */
    private Comment updatedCommentFor(Person person) throws CommandException {
        Comment existing = person.getComment();
        if (isReplacing) {
            return comment;
        }
        int combinedLength = existing.lengthAfterAppending(comment);
        if (combinedLength > Comment.MAX_LENGTH) {
            throw new CommandException(String.format(Comment.MESSAGE_TOO_LONG, combinedLength));
        }
        return existing.append(comment);
    }

    private String successMessage(String name, Comment updatedComment) {
        if (isClearing()) {
            return String.format(MESSAGE_CLEAR_SUCCESS, name);
        }
        return String.format(isReplacing ? MESSAGE_REPLACE_SUCCESS : MESSAGE_APPEND_SUCCESS,
                name, updatedComment);
    }

    private boolean isClearing() {
        return isReplacing && comment.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommentCommand otherCommentCommand)) {
            return false;
        }

        return index.equals(otherCommentCommand.index)
                && comment.equals(otherCommentCommand.comment)
                && isReplacing == otherCommentCommand.isReplacing;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("comment", comment)
                .add("isReplacing", isReplacing)
                .toString();
    }
}
