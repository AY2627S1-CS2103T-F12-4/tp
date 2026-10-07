package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses an optional tutorial-group filter for the list command.
     *
     * @throws ParseException if the arguments or group code are invalid.
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_GROUP);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP);
        if (arguments.getValue(PREFIX_GROUP).isEmpty()) {
            return new ListCommand();
        }
        return new ListCommand(ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).get()));
    }
}
