package seedu.address.logic.parser;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an export destination and optional tutorial group. */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) throws ParseException {
        var arguments = new CsvFileArgumentsParser(ExportCommand.MESSAGE_USAGE).parse(args);
        return new ExportCommand(arguments.path(), arguments.group());
    }
}
