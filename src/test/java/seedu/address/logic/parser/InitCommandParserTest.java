package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.InitCommand;
import seedu.address.model.person.Group;

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
        assertParseFailure(parser, "T09", "Unexpected text 'T09' before the parameters. Remove it.\n"
                + InitCommand.MESSAGE_USAGE);
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
                "Unexpected text 'unexpected' before the parameters. Remove it.\n" + InitCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_unsupportedPrefix_identifiesParameter() {
        for (String input : List.of(" grp/T09 w/5", " w/5 grp/T09", " grp/T09\tw/5", " grp/T09\u00a0w/5")) {
            assertParseFailure(parser, input, "Unsupported parameter 'w/'. Remove it.\n" + InitCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_extraText_identifiesUnexpectedText() {
        assertParseFailure(parser, " grp/T09 extra", "Unexpected text 'extra' after grp/. Remove it.\n"
                + InitCommand.MESSAGE_USAGE);
    }
}
