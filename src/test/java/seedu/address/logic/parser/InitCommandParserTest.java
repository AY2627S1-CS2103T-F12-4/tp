package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.InitCommand;
import seedu.address.model.group.Group;

public class InitCommandParserTest {

    private final InitCommandParser parser = new InitCommandParser();

    @Test
    public void parse_validGroup_success() {
        assertParseSuccess(parser, " grp/T09", new InitCommand(new Group("T09")));
        assertParseSuccess(parser, " grp/t09", new InitCommand(new Group("T09")));
    }

    @Test
    public void parse_missingGroup_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, InitCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "T09", expectedMessage);
    }

    @Test
    public void parse_repeatedGroup_failure() {
        assertParseFailure(parser, " grp/T09 grp/T10",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));
    }

    @Test
    public void parse_invalidGroup_failure() {
        assertParseFailure(parser, " grp/T9", Group.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, "unexpected grp/T09",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, InitCommand.MESSAGE_USAGE));
    }
}
