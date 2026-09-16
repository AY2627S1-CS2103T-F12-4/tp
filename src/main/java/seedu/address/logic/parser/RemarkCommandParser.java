package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

/**
 * Converts remark command arguments into a command.
 */
public class RemarkCommandParser implements Parser<RemarkCommand> {

    @Override
    public RemarkCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap tokens = ArgumentTokenizer.tokenize(args, PREFIX_REMARK);
        Index targetIndex;

        try {
            targetIndex = ParserUtil.parseIndex(tokens.getPreamble());
        } catch (ParseException exception) {
            String feedback = String.format(
                    Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    RemarkCommand.MESSAGE_USAGE);
            throw new ParseException(feedback, exception);
        }

        String remarkText = tokens.getValue(PREFIX_REMARK).orElse("");
        return new RemarkCommand(targetIndex, new Remark(remarkText));
    }
}
