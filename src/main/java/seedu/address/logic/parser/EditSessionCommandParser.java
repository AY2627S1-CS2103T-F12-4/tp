package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_GROUP, PREFIX_WEEK, PREFIX_DATE);
        if (arguments.getValue(PREFIX_GROUP).isEmpty() || arguments.getValue(PREFIX_WEEK).isEmpty()
                || arguments.getValue(PREFIX_DATE).isEmpty() || !arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditSessionCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_GROUP, PREFIX_WEEK, PREFIX_DATE);
        return new EditSessionCommand(new Session(ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).orElseThrow()),
                ParserUtil.parseWeek(arguments.getValue(PREFIX_WEEK).orElseThrow()),
                ParserUtil.parseSessionDate(arguments.getValue(PREFIX_DATE).orElseThrow())));
    }
}
