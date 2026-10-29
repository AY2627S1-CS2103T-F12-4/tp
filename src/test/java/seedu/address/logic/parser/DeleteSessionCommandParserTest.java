package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteSessionCommand;
import seedu.address.model.person.Group;
import seedu.address.model.session.Week;

public class DeleteSessionCommandParserTest {
    private final DeleteSessionCommandParser parser = new DeleteSessionCommandParser();

    @Test
    public void parse_validFieldsInAnyOrder_success() {
        DeleteSessionCommand expected = new DeleteSessionCommand(new Group("T09"), new Week(5));
        assertParseSuccess(parser, " grp/T09 w/5", expected);
        assertParseSuccess(parser, " w/5 grp/t09 ", expected);
    }

    @Test
    public void parse_missingFields_failure() {
        for (String args : List.of("", " grp/T09", " w/5")) {
            assertParseFailure(parser, args, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    DeleteSessionCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_repeatedFields_failure() {
        assertParseFailure(parser, " grp/T09 grp/T10 w/5",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_GROUP));
        assertParseFailure(parser, " grp/T09 w/5 w/6",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_WEEK));
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " grp/T9 w/5", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/ w/5", Group.MESSAGE_CONSTRAINTS);
        for (String week : List.of("", "0", "14", "05", "five")) {
            assertParseFailure(parser, " grp/T09 w/" + week, Week.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedPrefix_namesThePrefixAndShowsUsage() {
        for (String prefix : List.of("d/", "x/", "s/")) {
            for (String args : List.of(" " + prefix + "value grp/T09 w/5",
                    " grp/T09 " + prefix + "value w/5", " grp/T09 w/5 " + prefix + "value")) {
                assertParseFailure(parser, args, "Unsupported parameter '" + prefix + "'. Remove it.\n"
                        + DeleteSessionCommand.MESSAGE_USAGE);
            }
        }
    }

    @Test
    public void parse_extraText_namesTheTextAndItsLocation() {
        assertParseFailure(parser, " all grp/T09 w/5",
                "Unexpected text 'all' before the parameters. Remove it.\n" + DeleteSessionCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " grp/T09 extra w/5",
                "Unexpected text 'extra' after grp/. Remove it.\n" + DeleteSessionCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " grp/T09 w/5 extra words",
                "Unexpected text 'extra words' after w/. Remove it.\n" + DeleteSessionCommand.MESSAGE_USAGE);
    }
}
