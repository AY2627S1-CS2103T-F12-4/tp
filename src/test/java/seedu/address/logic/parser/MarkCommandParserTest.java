package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.model.attendance.Status;
import seedu.address.model.session.Week;

public class MarkCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE);

    private final MarkCommandParser parser = new MarkCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        MarkCommand expectedCommand = new MarkCommand(INDEX_THIRD_PERSON, new Week(5), Status.PRESENT);

        assertParseSuccess(parser, "3 w/5 s/present", expectedCommand);

        // parameters in a different order
        assertParseSuccess(parser, "3 s/present w/5", expectedCommand);

        // status in a different letter case, with extra whitespace
        assertParseSuccess(parser, "  3   w/ 5   s/ PRESENT ", expectedCommand);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        // no parameters
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);

        // missing index
        assertParseFailure(parser, "w/5 s/present", MESSAGE_INVALID_FORMAT);

        // missing week
        assertParseFailure(parser, "3 s/present", MESSAGE_INVALID_FORMAT);

        // missing status
        assertParseFailure(parser, "3 w/5", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // zero index
        assertParseFailure(parser, "0 w/5 s/present", MESSAGE_INVALID_FORMAT);

        // negative index
        assertParseFailure(parser, "-1 w/5 s/present", MESSAGE_INVALID_FORMAT);

        // non-numeric index
        assertParseFailure(parser, "three w/5 s/present", MESSAGE_INVALID_FORMAT);

        // more than one index
        assertParseFailure(parser, "3 4 w/5 s/present", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid week
        assertParseFailure(parser, "3 w/14 s/present", Week.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "3 w/five s/present", Week.MESSAGE_CONSTRAINTS);

        // invalid status
        assertParseFailure(parser, "3 w/5 s/late", Status.MESSAGE_CONSTRAINTS);

        // empty values
        assertParseFailure(parser, "3 w/ s/present", Week.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "3 w/5 s/", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedValue_failure() {
        // repeated week
        assertParseFailure(parser, "3 w/5 w/6 s/present", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_WEEK));

        // repeated status
        assertParseFailure(parser, "3 w/5 s/present s/absent",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));

        // both repeated
        assertParseFailure(parser, "3 w/5 s/present w/6 s/absent",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_WEEK, PREFIX_STATUS));
    }
}
