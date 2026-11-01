package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.KEYWORD_ALL;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteAllCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object, or a DeleteAllCommand object if
 * {@code all} is given in place of an index.
 */
public class DeleteCommandParser implements Parser<Command> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand or DeleteAllCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parse(String args) throws ParseException {
        // Letter case is ignored, as it is for statuses and group codes
        if (StringUtil.stripWhitespace(args).equalsIgnoreCase(KEYWORD_ALL)) {
            return new DeleteAllCommand();
        }
        try {
            Index index = ParserUtil.parseIndex(args);
            return new DeleteCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

}
