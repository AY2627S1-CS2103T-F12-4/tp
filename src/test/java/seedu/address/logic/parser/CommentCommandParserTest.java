package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommentCommand;
import seedu.address.model.person.Comment;

public class CommentCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, CommentCommand.MESSAGE_USAGE);

    private CommentCommandParser parser = new CommentCommandParser();

    @Test
    public void parse_appendWithText_returnsAppendingCommand() {
        assertParseSuccess(parser, "1 c/attentive",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("attentive"), false));
    }

    @Test
    public void parse_replaceWithText_returnsReplacingCommand() {
        assertParseSuccess(parser, "1 c-/starts strong",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("starts strong"), true));
    }

    @Test
    public void parse_replaceWithoutText_returnsClearingCommand() {
        assertParseSuccess(parser, "1 c-/",
                new CommentCommand(INDEX_FIRST_PERSON, Comment.EMPTY, true));
        assertParseSuccess(parser, "1 c-/   ",
                new CommentCommand(INDEX_FIRST_PERSON, Comment.EMPTY, true));
    }

    @Test
    public void parse_surroundingWhitespace_isNormalized() {
        assertParseSuccess(parser, "  1   c/  attentive   in  tutorials  ",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("attentive in tutorials"), false));
    }

    @Test
    public void parse_prefixesDoNotCollide_replaceIsNotReadAsAppend() {
        // " c/" must not be found inside " c-/": the character after c is a hyphen, not a slash
        assertParseSuccess(parser, "1 c-/attentive",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("attentive"), true));
    }

    @Test
    public void parse_noPrefix_throwsParseException() {
        assertParseFailure(parser, "1", INVALID_FORMAT);
        assertParseFailure(parser, "1 attentive", INVALID_FORMAT);
    }

    @Test
    public void parse_bothPrefixes_throwsParseException() {
        assertParseFailure(parser, "1 c/attentive c-/starts strong",
                CommentCommandParser.MESSAGE_MULTIPLE_PREFIXES);
    }

    @Test
    public void parse_prefixInsideComment_explainsHowToWriteIt() {
        // the user means one comment, but the space before c/ or c-/ starts another prefix
        assertParseFailure(parser, "1 c/ask about c/c++", CommentCommandParser.MESSAGE_MULTIPLE_PREFIXES);
        assertParseFailure(parser, "1 c/ask about c-/x", CommentCommandParser.MESSAGE_MULTIPLE_PREFIXES);
        assertParseFailure(parser, "1 c-/ask about c-/x", CommentCommandParser.MESSAGE_MULTIPLE_PREFIXES);
    }

    @Test
    public void parse_prefixWithoutSpaceBefore_isPartOfComment() {
        assertParseSuccess(parser, "1 c/ask about (c/c++)",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("ask about (c/c++)"), false));
        assertParseSuccess(parser, "1 c/c/c++ is hard",
                new CommentCommand(INDEX_FIRST_PERSON, new Comment("c/c++ is hard"), false));
    }

    @Test
    public void parse_emptyAppend_pointsAtTheClearPrefix() {
        assertParseFailure(parser, "1 c/", CommentCommandParser.MESSAGE_EMPTY_APPEND);
        assertParseFailure(parser, "1 c/   ", CommentCommandParser.MESSAGE_EMPTY_APPEND);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "1 c/attentive c/asks questions",
                CommentCommandParser.MESSAGE_MULTIPLE_PREFIXES);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "0 c/attentive", INVALID_FORMAT);
        assertParseFailure(parser, "-1 c/attentive", INVALID_FORMAT);
        assertParseFailure(parser, "abc c/attentive", INVALID_FORMAT);
    }

    @Test
    public void parse_commentOverLimit_throwsParseException() {
        assertParseFailure(parser, "1 c/" + "a".repeat(Comment.MAX_LENGTH + 1), Comment.MESSAGE_CONSTRAINTS);
    }
}
