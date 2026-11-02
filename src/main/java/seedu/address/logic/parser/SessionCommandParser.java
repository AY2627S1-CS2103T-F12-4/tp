package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import seedu.address.logic.commands.SessionCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

/**
 * Parses arguments for {@link SessionCommand}.
 */
public class SessionCommandParser implements Parser<SessionCommand> {

    @Override
    public SessionCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = SingleValueArgumentsParser.parse(args, SessionCommand.MESSAGE_USAGE,
                PREFIX_GROUP, PREFIX_WEEK, PREFIX_DATE);
        Group group = ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow());
        Week week = ParserUtil.parseWeek(arguments.getValue(PREFIX_WEEK).orElseThrow());
        SessionDate date = ParserUtil.parseSessionDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        return new SessionCommand(new Session(group, week, date));
    }
}
