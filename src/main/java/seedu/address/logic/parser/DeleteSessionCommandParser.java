package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import seedu.address.logic.commands.DeleteSessionCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the group and teaching week identifying a session to delete. */
public class DeleteSessionCommandParser implements Parser<DeleteSessionCommand> {
    @Override
    public DeleteSessionCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = SessionArgumentsParser.parse(args, DeleteSessionCommand.MESSAGE_USAGE,
                PREFIX_GROUP, PREFIX_WEEK);
        return new DeleteSessionCommand(ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow()),
                ParserUtil.parseWeek(arguments.getValue(PREFIX_WEEK).orElseThrow()));
    }
}
