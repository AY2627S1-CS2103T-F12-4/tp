package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditSessionCommand;
import seedu.address.model.person.Group;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;

public class EditSessionCommandParserTest {
    private final EditSessionCommandParser parser = new EditSessionCommandParser();

    @Test
    public void parse_validFieldsInAnyOrder_success() {
        EditSessionCommand expected = new EditSessionCommand(
                new Session(new Group("T09"), new Week(5), new SessionDate("2026-09-16")));
        assertParseSuccess(parser, " grp/T09 w/5 d/2026-09-16", expected);
        assertParseSuccess(parser, " d/2026-09-16 w/5 grp/t09 ", expected);
    }

    @Test
    public void parse_missingFieldsOrPreamble_failure() {
        for (String args : List.of("", " grp/T09 w/5", " grp/T09 d/2026-09-16", " w/5 d/2026-09-16",
                " extra grp/T09 w/5 d/2026-09-16")) {
            assertParseFailure(parser, args, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    EditSessionCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_repeatedFields_failure() {
        assertParseFailure(parser, " grp/T09 grp/T10 w/5 d/2026-09-16",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_GROUP));
        assertParseFailure(parser, " grp/T09 w/5 w/6 d/2026-09-16",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_WEEK));
        assertParseFailure(parser, " grp/T09 w/5 d/2026-09-16 d/2026-09-17",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }

    @Test
    public void parse_invalidOrEmptyValues_failure() {
        assertParseFailure(parser, " grp/T9 w/5 d/2026-09-16", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/ w/5 d/2026-09-16", Group.MESSAGE_CONSTRAINTS);
        for (String week : List.of("", "0", "14", "05", "five")) {
            assertParseFailure(parser, " grp/T09 w/" + week + " d/2026-09-16", Week.MESSAGE_CONSTRAINTS);
        }
        for (String date : List.of("", "2026-02-29", "2026-09-16 x/value")) {
            assertParseFailure(parser, " grp/T09 w/5 d/" + date, SessionDate.MESSAGE_CONSTRAINTS);
        }
    }
}
