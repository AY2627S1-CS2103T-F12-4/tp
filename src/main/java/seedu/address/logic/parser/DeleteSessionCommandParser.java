package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import seedu.address.logic.commands.DeleteSessionCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the group and teaching week identifying a session to delete. */
public class DeleteSessionCommandParser implements Parser<DeleteSessionCommand> {
    @Override
    public DeleteSessionCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_GROUP, PREFIX_WEEK);
        if (arguments.getValue(PREFIX_GROUP).isEmpty() || arguments.getValue(PREFIX_WEEK).isEmpty()
                || !arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteSessionCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP, PREFIX_WEEK);
        return new DeleteSessionCommand(ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow()),
                ParserUtil.parseWeek(arguments.getValue(PREFIX_WEEK).orElseThrow()));
    }
}
