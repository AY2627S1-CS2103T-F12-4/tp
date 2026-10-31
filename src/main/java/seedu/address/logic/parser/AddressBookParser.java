package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommentCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteSessionCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditSessionCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.InitCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SessionCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Separates the command word from arguments, leaving argument whitespace to each command parser.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("\\s*(?<commandWord>\\S+)(?<arguments>.*)",
            Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL);
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        if ((commandWord.equals(ExitCommand.COMMAND_WORD) || commandWord.equals(HelpCommand.COMMAND_WORD))
                && !arguments.matches("(?U)\\s*")) {
            String usage = commandWord.equals(ExitCommand.COMMAND_WORD)
                    ? ExitCommand.MESSAGE_USAGE : HelpCommand.MESSAGE_USAGE;
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }

        // Only search normalizes line breaks within its arguments.
        if (!commandWord.equals(FindCommand.COMMAND_WORD) && !arguments.matches(".*")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case CommentCommand.COMMAND_WORD -> new CommentCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ExportCommand.COMMAND_WORD -> new ExportCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            case InitCommand.COMMAND_WORD -> new InitCommandParser().parse(arguments);
            case SessionCommand.COMMAND_WORD -> new SessionCommandParser().parse(arguments);
            case EditSessionCommand.COMMAND_WORD -> new EditSessionCommandParser().parse(arguments);
            case DeleteSessionCommand.COMMAND_WORD -> new DeleteSessionCommandParser().parse(arguments);
            case MarkCommand.COMMAND_WORD -> new MarkCommandParser().parse(arguments);
            case RedoCommand.COMMAND_WORD -> new RedoCommandParser().parse(arguments);
            case UndoCommand.COMMAND_WORD -> new UndoCommandParser().parse(arguments);
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
