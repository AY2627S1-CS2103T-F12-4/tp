package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import seedu.address.logic.commands.EditSessionCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.session.Session;

/** Parses the group, week and replacement date for a session edit. */
public class EditSessionCommandParser implements Parser<EditSessionCommand> {
    @Override
    public EditSessionCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = SessionArgumentsParser.parse(args, EditSessionCommand.MESSAGE_USAGE,
                PREFIX_GROUP, PREFIX_WEEK, PREFIX_DATE);
        return new EditSessionCommand(new Session(ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow()),
                ParserUtil.parseWeek(arguments.getValue(PREFIX_WEEK).orElseThrow()),
                ParserUtil.parseSessionDate(arguments.getValue(PREFIX_DATE).orElseThrow())));
    }
}
