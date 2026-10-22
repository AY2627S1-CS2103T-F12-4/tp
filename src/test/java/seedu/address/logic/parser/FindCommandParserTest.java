package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.NameContainsQueryPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_missingOrEmptyQuery_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : new String[]{"", "     ", " Alice Bob", " n/", " n/ \t ", " Alice n/Bob"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_duplicateNamePrefixes_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME);
        assertParseFailure(parser, " n/Alice n/Bob", expectedMessage);
        assertParseFailure(parser, " n/Alice\tn/Bob", expectedMessage);
        assertParseFailure(parser, " n/Alice n/", expectedMessage);
        assertParseFailure(parser, " n/ n/Bob", expectedMessage);
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        for (String input : new String[]{" x/T09", " n/John\te/john@example.com",
            " n/John z/value", " N/John", " n/John x_2/value", " m/A0123456X w/1"}) {
            assertParseFailure(parser, input, FindCommand.MESSAGE_UNKNOWN_PARAMETER);
        }
    }

    @Test
    public void parse_invalidQuery_throwsParseException() {
        for (String query : new String[]{"123", "John2", ".'-", "John@Tan", "John/Tan", "John_Tan",
            "John\u0000Tan", "a".repeat(101)}) {
            assertParseFailure(parser, " n/" + query, NameContainsQueryPredicate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_singleKeyword_returnsFindCommand() {
        FindCommand expected = new FindCommand(new NameContainsQueryPredicate("John"));
        assertParseSuccess(parser, " n/John", expected);
        assertParseSuccess(parser, "n/John", expected);
    }

    @Test
    public void parse_phraseAndWhitespace_returnsFindCommand() {
        FindCommand expected = new FindCommand(new NameContainsQueryPredicate("Alice Bob"));
        assertParseSuccess(parser, " n/Alice Bob", expected);
        assertParseSuccess(parser, " \n \t n/ Alice \n \t Bob  \t", expected);
        assertParseSuccess(parser, "\u00a0n/Alice\u2003Bob\u00a0", expected);
    }

    @Test
    public void parse_supportedCharactersAndLength_returnsFindCommand() {
        for (String query : new String[]{"A", "a".repeat(100), "Anne-Marie", "O'Neil", "O\u2019Neil",
            "J. Tan", "\u00c9lodie", "E\u0301lodie"}) {
            assertParseSuccess(parser, " n/" + query, new FindCommand(new NameContainsQueryPredicate(query)));
        }
    }

    @Test
    public void parse_fieldFilters_normalizesValuesAndAcceptsAnyOrder() {
        FindCommand expected = new FindCommand(new NameContainsQueryPredicate("John Tan"),
                new Matric("A0123456X"), new Group("T09"));
        assertParseSuccess(parser, " n/ John   Tan m/a0123456x grp/t09", expected);
        assertParseSuccess(parser, " grp/ t09\t m/a0123456x\u00a0n/John\nTan", expected);
        assertParseSuccess(parser, " m/a0123456x", new FindCommand(null, new Matric("A0123456X"), null));
        assertParseSuccess(parser, " grp/t09", new FindCommand(null, null, new Group("T09")));
    }

    @Test
    public void parse_blankOrMalformedFilters_rejectsInput() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : new String[]{" m/", " grp/ ", " n/John m/", " grp/T09 n/", "oops grp/T09"}) {
            assertParseFailure(parser, input, usage);
        }
        assertParseFailure(parser, " m/A0123", Matric.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " grp/T9", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/A0123456X m/A0123457X",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_MATRIC));
        assertParseFailure(parser, " grp/T09\tgrp/T10",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_GROUP));
    }
}
