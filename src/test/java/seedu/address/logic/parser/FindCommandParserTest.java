package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
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
        for (String input : new String[]{" grp/T09", " m/A0123456X", " n/John grp/T09",
            " n/John\te/john@example.com", " grp/T09 n/John", " n/John z/value", " N/John"}) {
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
}
