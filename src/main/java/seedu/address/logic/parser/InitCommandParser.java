package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import seedu.address.logic.commands.InitCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;

/**
 * Parses arguments for {@link InitCommand}.
 */
public class InitCommandParser implements Parser<InitCommand> {

    @Override
    public InitCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = SingleValueArgumentsParser.parse(args, InitCommand.MESSAGE_USAGE, PREFIX_GROUP);
        Group group = ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow());
        return new InitCommand(group);
    }
}
