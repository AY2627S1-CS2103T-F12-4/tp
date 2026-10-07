package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.Group;

public class ListCommandParserTest {
    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_withoutGroup_listsAll() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, " \t ", new ListCommand());
    }

    @Test
    public void parse_group_normalizesCaseAndWhitespace() {
        assertParseSuccess(parser, " grp/T09", new ListCommand(new Group("T09")));
        assertParseSuccess(parser, "  grp/ tg01  ", new ListCommand(new Group("TG01")));
    }

    @Test
    public void parse_repeatedGroup_rejectedEvenIfIdentical() {
        assertParseFailure(parser, " grp/T09 grp/T09",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));
    }

    @Test
    public void parse_invalidGroup_rejected() {
        for (String args : new String[]{" grp/", " grp/T9", " grp/T 09", " grp/T09 n/Alice", " grp/T09 xyz"}) {
            assertParseFailure(parser, args, Group.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unexpectedArgument_rejected() {
        for (String args : new String[]{" Alice", " 3", " n/Alice", " Alice grp/T09"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }
    }
}
