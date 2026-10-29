package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommentCommand;
import seedu.address.logic.commands.DeleteAllCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.InitCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MarkAllCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.SessionCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsQueryPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand("  clear \t ") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_clearWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        for (String arguments : List.of("grp/T91", "all", "3", "unexpected text")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("clear " + arguments));
        }
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_comment() throws Exception {
        CommentCommand command = (CommentCommand) parser.parseCommand(
                CommentCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " c/attentive");
        assertEquals(new CommentCommand(INDEX_FIRST_PERSON, new Comment("attentive"), false), command);
    }

    @Test
    public void parseCommand_commentReplace() throws Exception {
        CommentCommand command = (CommentCommand) parser.parseCommand(
                CommentCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " c-/");
        assertEquals(new CommentCommand(INDEX_FIRST_PERSON, Comment.EMPTY, true), command);
    }

    @Test
    public void parseCommand_deleteAll() throws Exception {
        assertEquals(new DeleteAllCommand(), parser.parseCommand("delete all"));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        FindCommand command = (FindCommand) parser.parseCommand(FindCommand.COMMAND_WORD + " n/John Tan");
        assertEquals(new FindCommand(new NameContainsQueryPredicate("John Tan")), command);
    }

    @Test
    public void parseCommand_findWithUnicodeWhitespace_returnsNormalizedSearch() throws Exception {
        FindCommand expected = new FindCommand(new NameContainsQueryPredicate("John Tan"));
        for (String command : List.of("find\u00a0n/John\u2003Tan", "\u00a0find n/John Tan\u00a0",
                "find\u2003n/John Tan")) {
            assertEquals(expected, parser.parseCommand(command));
        }
    }

    @Test
    public void parseCommand_findWithLineBreaks_returnsNormalizedSearch() throws Exception {
        FindCommand expected = new FindCommand(new NameContainsQueryPredicate("John Tan"));
        for (String whitespace : List.of("\n", "\r\n", "\u0085", "\u2028", "\u2029")) {
            assertEquals(expected, parser.parseCommand("find n/John" + whitespace + "Tan"));
        }
    }

    @Test
    public void parseCommand_findWithWhitespace_keepsQueryValidation() {
        assertThrows(ParseException.class, FindCommand.MESSAGE_UNKNOWN_PARAMETER, () ->
                parser.parseCommand("find\u00a0n/John\u2003e/john@example.com"));
        assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME), () ->
                parser.parseCommand("find n/John\n n/Tan"));
        assertThrows(ParseException.class, NameContainsQueryPredicate.MESSAGE_CONSTRAINTS, () ->
                parser.parseCommand("find\u2003n/John2"));
    }

    @Test
    public void parseCommand_horizontalWhitespace_matchesSpaceDelimitedCommands() throws Exception {
        for (String whitespace : List.of("\t", "\u00a0", "\u2003", "\u202f", "\u3000")) {
            for (String command : List.of("init grp/T11", "session grp/T09 w/2 d/2026-10-27",
                    "edit 1 n/Alicia Tan", "mark 1 w/1 s/present", "list grp/T09")) {
                assertEquals(parser.parseCommand(command),
                        parser.parseCommand(whitespace + command.replace(" ", whitespace) + whitespace));
            }
            String duplicateGroupMessage = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_GROUP);
            assertThrows(ParseException.class, duplicateGroupMessage, () ->
                    parser.parseCommand("init" + whitespace + "grp/T09" + whitespace + "grp/T10"));
            assertThrows(ParseException.class, EditCommand.MESSAGE_UNKNOWN_PARAMETER, () ->
                    parser.parseCommand("edit 1 n/Alicia" + whitespace + "x/value"));
        }
    }

    @Test
    public void parseCommand_horizontalWhitespaceAroundValues_usesExistingFieldRules() throws Exception {
        assertEquals(new InitCommand(new Group("T11")), parser.parseCommand("init\u00a0grp/\u00a0T11\u00a0"));
        String comment = "Bring\u00a0notes\tplease";
        assertEquals(new CommentCommand(INDEX_FIRST_PERSON, new Comment(comment), false),
                parser.parseCommand("comment\t1\u00a0c/" + comment));
    }

    @Test
    public void parseCommand_nonSearchWithLineBreaks_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        for (String command : List.of("clear", "exit", "help", "add", "list", "init", "session", "edit", "mark",
                "unknownCommand")) {
            for (String lineBreak : List.of("\n", "\r\n", "\u0085", "\u2028", "\u2029")) {
                assertThrows(ParseException.class, expectedMessage, () ->
                        parser.parseCommand(command + lineBreak + "other input"));
            }
        }
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand("  list \t ") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        for (String arguments : List.of("3", "John", "n/John")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("list " + arguments));
        }
    }

    @Test
    public void parseCommand_mark() throws Exception {
        MarkCommand command = (MarkCommand) parser.parseCommand(MarkCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " w/5 s/absent");
        assertEquals(new MarkCommand(INDEX_FIRST_PERSON, new Week(5), Status.ABSENT), command);
    }

    @Test
    public void parseCommand_markAll() throws Exception {
        assertEquals(new MarkAllCommand(new Week(5), Status.PRESENT), parser.parseCommand("mark all w/5 s/present"));
    }

    @Test
    public void parseCommand_init() throws Exception {
        InitCommand command = (InitCommand) parser.parseCommand("init grp/t09");
        assertEquals(new InitCommand(new Group("T09")), command);
    }

    @Test
    public void parseCommand_session() throws Exception {
        SessionCommand command = (SessionCommand) parser.parseCommand(
                "session grp/T09 w/5 d/2026-09-15");
        Session expectedSession = new Session(new Group("T09"), new Week(5), new SessionDate("2026-09-15"));
        assertEquals(new SessionCommand(expectedSession), command);
    }

    @Test
    public void parseCommand_undo() throws Exception {
        assertTrue(parser.parseCommand(UndoCommand.COMMAND_WORD) instanceof UndoCommand);
    }

    @Test
    public void parseCommand_undoWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("undo 1"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unicodeWhitespaceOnly_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand("\u00a0\u2003\n"));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () ->
                parser.parseCommand("\u00a0unknownCommand\u2003"));
    }
}
