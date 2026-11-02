package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

public class ExitHelpArgumentsTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_extraArguments_reportsCommandSpecificUsage() {
        for (String command : List.of("exit", "help")) {
            for (String separator : List.of(" ", "\t", "\u00a0", "\u2007", "\u202f", "\n", "\r\n")) {
                for (String argument : List.of("3", "unexpected", "grp/T81", "all", "?")) {
                    ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(
                            command + separator + argument));
                    assertTrue(error.getMessage().contains(command + ":"));
                    assertTrue(error.getMessage().contains("takes no arguments"));
                }
            }
        }
    }

    @Test
    public void parseCommand_generalErrors_doNotClaimCommandsTakeNoArguments() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "help: Shows program usage instructions.\nExample: help");
        for (String input : List.of("", "\u00a0\u2003\n", "list grp/\nT81", "add n/Beta\nStudent")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(expected, error.getMessage());
            assertFalse(error.getMessage().contains("takes no arguments"));
        }
    }

    @Test
    public void parseCommand_whitespaceOnly_preservesValidActionFlags() throws Exception {
        for (String whitespace : List.of("", " ", "\t", "\u00a0", "\u2007", "\u202f", "\u2003")) {
            CommandResult exit = parser.parseCommand(whitespace + "exit" + whitespace).execute(new ModelManager());
            assertTrue(exit.isExit());
            assertFalse(exit.isShowHelp());
            CommandResult help = parser.parseCommand(whitespace + "help" + whitespace).execute(new ModelManager());
            assertTrue(help.isShowHelp());
            assertFalse(help.isExit());
        }
    }
}
