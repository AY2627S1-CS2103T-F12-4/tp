package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteAllCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;

public class UnicodeWhitespaceParserTest {
    private static final List<String> SPACES = List.of(" ", "\t", "\u00a0", "\u2007", "\u202f", "\u2003");

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_boundaryWhitespace_acceptsAffectedCommands() throws Exception {
        List<String> commands = List.of("clear", "undo", "redo", "delete 1", "delete all",
                "export f/attendance.csv", "export grp/T81 f/attendance.csv");
        for (String space : SPACES) {
            for (String command : commands) {
                Class<?> expectedClass = parser.parseCommand(command).getClass();
                assertEquals(expectedClass, parser.parseCommand(space + command + space).getClass());
                assertEquals(expectedClass, parser.parseCommand(command.replace(" ", space) + space).getClass());
            }
            assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("delete" + space + "1" + space));
            assertEquals(new DeleteAllCommand(), parser.parseCommand("delete" + space + "ALL" + space));
        }
    }

    @Test
    public void parseCommand_export_trimsOnlyOutsideQuotedPath() throws Exception {
        Path path = Path.of("\u00a0reports/A  B\u00a0C grp/T81.csv");
        ExportCommand expected = new ExportCommand(path, new Group("T81"));
        for (String space : SPACES) {
            assertEquals(expected, parser.parseCommand("export" + space + "f/" + space + "\"" + path + "\""
                    + space + "grp/" + space + "T81" + space));
            assertEquals(new ExportCommand(Path.of("A\u00a0B.csv"), null),
                    parser.parseCommand("export" + space + "f/" + space + "A\u00a0B.csv" + space));
        }
    }

    @Test
    public void parseCommand_invalidArgumentsAndSplitIndexes_stillRejected() {
        for (String space : SPACES) {
            for (String command : List.of("clear", "undo", "redo")) {
                assertThrows(ParseException.class, () -> parser.parseCommand(command + space + "unexpected"));
            }
            assertThrows(ParseException.class, () -> parser.parseCommand("delete" + space + "1" + space + "2"));
            assertThrows(ParseException.class, () -> parser.parseCommand("delete" + space + "all" + space + "extra"));
            assertThrows(ParseException.class, () -> parser.parseCommand("export" + space + "f/" + space));
            assertThrows(ParseException.class, () -> parser.parseCommand(
                    "export" + space + "extra" + space + "f/a.csv"));
            assertThrows(ParseException.class, () -> parser.parseCommand("export" + space + "f/\"" + space + "\""));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("export f/\"a\nb.csv\""));
        assertThrows(ParseException.class, () -> parser.parseCommand("clear\nunexpected"));
    }
}
