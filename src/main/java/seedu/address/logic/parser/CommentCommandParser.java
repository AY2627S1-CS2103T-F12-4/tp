package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMENT_REPLACE;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.CommentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Comment;

/**
 * Parses input arguments and creates a new CommentCommand object.
 */
public class CommentCommandParser implements Parser<CommentCommand> {

    public static final String MESSAGE_BOTH_PREFIXES =
            "Use either " + PREFIX_COMMENT + " to add to the comment or "
                    + PREFIX_COMMENT_REPLACE + " to replace it, not both.";

    public static final String MESSAGE_EMPTY_APPEND =
            "There is nothing to add. Use " + PREFIX_COMMENT_REPLACE + " on its own to clear the comment.";

    /**
     * Parses the given {@code String} of arguments in the context of the CommentCommand
     * and returns a CommentCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public CommentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_COMMENT, PREFIX_COMMENT_REPLACE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, CommentCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_COMMENT, PREFIX_COMMENT_REPLACE);

        Optional<String> addition = argMultimap.getValue(PREFIX_COMMENT);
        Optional<String> replacement = argMultimap.getValue(PREFIX_COMMENT_REPLACE);

        if (addition.isPresent() && replacement.isPresent()) {
            throw new ParseException(MESSAGE_BOTH_PREFIXES);
        }
        if (addition.isEmpty() && replacement.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, CommentCommand.MESSAGE_USAGE));
        }

        if (replacement.isPresent()) {
            // An empty value clears the comment, the way an empty t/ clears a student's tags.
            Comment comment = Comment.normalize(replacement.get()).isEmpty()
                    ? Comment.EMPTY
                    : ParserUtil.parseComment(replacement.get());
            return new CommentCommand(index, comment, true);
        }

        if (Comment.normalize(addition.get()).isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_APPEND);
        }
        return new CommentCommand(index, ParserUtil.parseComment(addition.get()), false);
    }
}
