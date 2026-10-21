package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;

public class ExportCommandParserTest {
    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_pathsAndOptionalGroup_preservesSpacesAndAcceptsEitherOrder() throws Exception {
        assertEquals(new ExportCommand(Path.of("attendance.csv"), null), parser.parse(" f/attendance.csv"));
        ExportCommand expected = new ExportCommand(Path.of("reports/T09  attendance.csv"), new Group("T09"));
        assertEquals(expected, parser.parse(" f/reports/T09  attendance.csv grp/t09"));
        assertEquals(expected, parser.parse(" grp/t09\tf/reports/T09  attendance.csv"));
        Path absolute = Path.of("attendance.CSV").toAbsolutePath();
        assertEquals(new ExportCommand(absolute, null), parser.parse("f/" + absolute));
    }

    @Test
    public void parse_quotedPath_keepsDirectoryNamesAndPrefixLikeFragments() throws Exception {
        Path path = Path.of("My reports/grp/T09 attendance.csv");
        assertEquals(new ExportCommand(path, new Group("T09")),
                parser.parse(" f/\"My reports/grp/T09 attendance.csv\" grp/T09"));
        assertEquals(new ExportCommand(Path.of("my grp/T09 report.csv"), null),
                parser.parse(" f/\"my grp/T09 report.csv\""));
    }

    @Test
    public void parse_missingBlankUnsupportedOrMalformedArguments_rejectsInput() {
        for (String input : List.of("", "grp/T09", "f/", "f/\"\"", "f/\"   \"", "report.csv",
                "oops f/report.csv", "f/report.txt", "f/report.csv grp/", "f/report.csv grp/T9",
                "f/report.csv x/value", "f/\"report.csv", "f/\"report.csv\" trailing",
                "f/report\".csv\"", "f/report\u0000.csv", "f/report.csv grp/\"T09\"")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void parse_repeatedPrefixes_rejectsRatherThanSilentlyChoosingOne() {
        assertEquals(Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_FILE),
                assertThrows(ParseException.class, () -> parser.parse("f/one.csv f/two.csv")).getMessage());
        assertEquals(Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_GROUP),
                assertThrows(ParseException.class, () ->
                    parser.parse("f/one.csv grp/T09 grp/T10")).getMessage());
    }

    @Test
    public void parse_invalidPathAndControlCharacters_reportClearErrors() {
        assertEquals(ExportCommand.MESSAGE_INVALID_PATH,
                assertThrows(ParseException.class, () -> parser.parse("f/a\u0000.csv")).getMessage());
        assertEquals(ExportCommand.MESSAGE_INVALID_PATH,
                assertThrows(ParseException.class, () -> parser.parse("f/\"a\tb.csv\"")).getMessage());
    }

    @Test
    public void parseCommand_dispatchesExportAndRejectsMultilinePaths() throws Exception {
        AddressBookParser commandParser = new AddressBookParser();
        assertEquals(new ExportCommand(Path.of("attendance.csv"), new Group("T09")),
                commandParser.parseCommand("export f/attendance.csv grp/T09"));
        assertThrows(ParseException.class, () -> commandParser.parseCommand("export f/a\nb.csv"));
    }
}
