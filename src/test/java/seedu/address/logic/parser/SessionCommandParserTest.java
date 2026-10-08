package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.SessionCommand;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class SessionCommandParserTest {

    private final SessionCommandParser parser = new SessionCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Session expectedSession = new Session(new Group("T09"), new Week(5), new SessionDate("2026-09-15"));
        assertParseSuccess(parser, " grp/T09 w/5 d/2026-09-15", new SessionCommand(expectedSession));
        assertParseSuccess(parser, " d/2026-09-15 grp/t09 w/5", new SessionCommand(expectedSession));
    }

    @Test
    public void parse_missingField_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SessionCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " grp/T09 w/5", expectedMessage);
        assertParseFailure(parser, " grp/T09 d/2026-09-15", expectedMessage);
        assertParseFailure(parser, " w/5 d/2026-09-15", expectedMessage);
    }

    @Test
    public void parse_repeatedField_failure() {
        assertParseFailure(parser, " grp/T09 grp/T10 w/5 d/2026-09-15",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));
        assertParseFailure(parser, " grp/T09 w/5 w/6 d/2026-09-15",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_WEEK));
        assertParseFailure(parser, " grp/T09 w/5 d/2026-09-15 d/2026-09-22",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }

    @Test
    public void parse_invalidField_failure() {
        assertParseFailure(parser, " grp/T9 w/5 d/2026-09-15", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/T09 w/0 d/2026-09-15", Week.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/T09 w/14 d/2026-09-15", Week.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/T09 w/five d/2026-09-15", Week.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/T09 w/5 d/2026-02-30", SessionDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, "unexpected grp/T09 w/5 d/2026-09-15",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SessionCommand.MESSAGE_USAGE));
    }
}
