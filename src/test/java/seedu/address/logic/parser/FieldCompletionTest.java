package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FieldCompletion.Suggestion;

public class FieldCompletionTest {
    private final List<String> groups = new ArrayList<>(List.of("T10", "T09", "TG01"));

    @Test
    public void suggest_prefixes_respectsCommandAndExistingFields() {
        assertEquals(List.of("grp/"), values("list gr"));
        assertEquals(List.of("grp/"), values("  list gr"));
        assertEquals(List.of("w/", "s/"), values("mark 1 "));
        assertEquals(List.of("w/", "d/"), values("session grp/T09 "));
        assertEquals(List.of("e/"), values("edit 1 e"));
        assertEquals(List.of("t/"), values("add t/friend t"));
        assertTrue(values("delete gr").isEmpty());
        assertTrue(values("unknown gr").isEmpty());
        assertTrue(values("list n").isEmpty());
        assertTrue(values("").isEmpty());
        assertTrue(values("list").isEmpty());
    }

    @Test
    public void suggest_commands_completesOnlyTheCommandAndPreservesArguments() {
        assertEquals(List.of("add"), values("ad"));
        assertEquals(List.of("edit", "editsession", "exit", "export"), values("e"));
        assertEquals(List.of("comment"), values("com"));
        assertEquals(List.of("undo"), values("un"));
        assertEquals(List.of("list"), values("LI"));
        assertEquals(List.of("session"), values("  ses"));
        assertTrue(values("nonsense").isEmpty());
        assertTrue(values("   ").isEmpty());
        String text = "li grp/T09";
        assertEquals("list grp/T09", FieldCompletion.suggest(text, 2, groups).getFirst().apply(text));
    }

    @Test
    public void suggest_commandWithTextAfterCaret_keepsThatTextInsteadOfReplacingIt() {
        String text = "comis";
        Suggestion selected = FieldCompletion.suggest(text, 3, groups).getFirst();
        assertEquals("commentis", selected.apply(text));
        assertEquals(0, selected.start());
        assertEquals(3, selected.end());

        String sentence = "comis great";
        assertEquals("commentis great",
                FieldCompletion.suggest(sentence, 3, groups).getFirst().apply(sentence));

        // Applying this would change nothing, so it is not offered
        assertTrue(FieldCompletion.suggest("commentis", "comment".length(), groups).isEmpty());
    }

    @Test
    public void suggest_values_matchesCaseInsensitivelyAndReadsCurrentGroups() {
        assertEquals(List.of("T09", "T10", "TG01"), values("list grp/t"));
        assertEquals(List.of("present", "absent"), values("mark 1 s/"));
        assertEquals(List.of("present"), values("mark 1 s/P"));
        assertTrue(values("list grp/T99").isEmpty());
        assertTrue(values("init grp/T").isEmpty());
        assertEquals(List.of("T09", "T10", "TG01"), values("find grp/T"));
        assertTrue(values("add n/Jo").isEmpty());
        assertTrue(values("mark 1 s/present").isEmpty());
        groups.add("T11");
        assertEquals(List.of("T10", "T11"), values("list grp/T1"));
        groups.clear();
        assertTrue(values("list grp/").isEmpty());
    }

    @Test
    public void suggest_groupValuesAfterWhitespace_supportsEveryGroupCompletionCommand() {
        for (String command : List.of("list", "find", "add", "edit 1", "session", "editsession",
                "deletesession", "export f/report.csv")) {
            for (String padding : List.of(" ", "   ", "\t", " \t ")) {
                assertEquals(List.of("T09", "T10", "TG01"), values(command + " grp/" + padding + "t"));
                assertEquals(List.of("T09", "T10", "TG01"), values(command + " grp/" + padding));
                assertTrue(values(command + " grp/" + padding + "T09").isEmpty());
            }
        }
        assertTrue(values("init grp/ T").isEmpty());
        assertTrue(values("list grp/ T99").isEmpty());
        groups.add("T11");
        assertEquals(List.of("T10", "T11"), values("list grp/ T1"));
        groups.clear();
        assertTrue(values("list grp/ ").isEmpty());
    }

    @Test
    public void suggest_statusValuesAfterWhitespace_keepsCaseInsensitiveMatching() {
        for (String padding : List.of(" ", "   ", "\t", " \t ")) {
            assertEquals(List.of("present", "absent"), values("mark 1 w/1 s/" + padding));
            assertEquals(List.of("present"), values("mark 1 w/1 s/" + padding + "P"));
            assertEquals(List.of("absent"), values("mark all w/1 s/" + padding + "a"));
            assertTrue(values("mark 1 w/1 s/" + padding + "present").isEmpty());
        }
    }

    @Test
    public void suggest_spacedValueAtCaret_preservesPaddingAndSurroundingArguments() {
        String session = "session grp/  T99 w/3 d/2026-10-19";
        Suggestion group = FieldCompletion.suggest(session, session.indexOf("T99") + 1, groups).getFirst();
        assertEquals("session grp/  T09 w/3 d/2026-10-19", group.apply(session));
        assertEquals(session.indexOf("T99"), group.start());
        assertEquals(session.indexOf("T99") + 3, group.end());
        String mark = "mark 1 s/ \taxxxx w/3";
        Suggestion status = FieldCompletion.suggest(mark, mark.indexOf("axxxx") + 1, groups).getFirst();
        assertEquals("mark 1 s/ \tabsent w/3", status.apply(mark));
        assertEquals(mark.indexOf("axxxx"), status.start());
        assertEquals(mark.indexOf("axxxx") + 5, status.end());
        String export = "export f/\"reports/grp/ T09.csv\" grp/ t";
        assertEquals("export f/\"reports/grp/ T09.csv\" grp/ T09",
                FieldCompletion.suggest(export, export.length(), groups).getFirst().apply(export));
    }

    @Test
    public void suggest_afterCompletedSpacedValues_stillOffersNewFieldsAndRespectsContext() {
        assertEquals(List.of("w/", "d/"), values("session grp/ T09 "));
        assertEquals(List.of("w/"), values("mark 1 s/ present "));
        assertTrue(values("list grp/ T09 ").isEmpty());
        assertTrue(values("export f/\"reports grp/ T").isEmpty());
        assertTrue(values("comment 1 c/grp/ T").isEmpty());
        assertTrue(values("mark 1 grp/ T").isEmpty());
        assertTrue(values("list grp/\u00a0T").isEmpty());
    }

    @Test
    public void suggest_studentFields_excludesRemovedPhoneAndAddress() {
        for (String command : List.of("add ", "edit 1 ")) {
            assertEquals(List.of("n/", "m/", "grp/", "e/", "t/"), values(command));
            assertTrue(values(command + "p").isEmpty());
            assertTrue(values(command + "a").isEmpty());
        }
        assertEquals(List.of("m/", "grp/"), values("find n/John "));
        assertEquals(List.of("grp/"), values("find n/John m/A0000001X "));
        assertTrue(values("find n/John m/A0000001X grp/T09 ").isEmpty());
    }

    @Test
    public void suggest_comments_offersOnlyOneAppendOrReplaceField() {
        assertEquals(List.of("c/", "c-/"), values("comment 1 c"));
        assertEquals(List.of("c-/"), values("comment 1 c-"));
        assertTrue(values("comment 1 c/asks questions c").isEmpty());
        assertTrue(values("comment 1 c-/new note c").isEmpty());
        assertTrue(values("comment 1 c/asks questions ").isEmpty());
        assertTrue(values("edit 1 c").isEmpty());
    }

    @Test
    public void suggest_all_onlyCompletesTheFirstMarkOrDeleteArgument() {
        assertEquals(List.of("all"), values("delete a"));
        assertEquals(List.of("all"), values("mark al"));
        assertEquals(List.of("w/", "s/"), values("mark all "));
        assertEquals(List.of("present"), values("mark all w/1 s/p"));
        assertTrue(values("delete all ").isEmpty());
        assertTrue(values("delete 1 a").isEmpty());
        assertTrue(values("mark 1 a").isEmpty());
        assertTrue(values("mark w/1 a").isEmpty());
        assertTrue(values("comment a").isEmpty());
        assertTrue(values("list a").isEmpty());
    }

    @Test
    public void suggest_export_ignoresQuotedPathsAndOffersUnusedFields() {
        assertEquals(List.of("f/", "grp/"), values("export "));
        assertEquals(List.of("grp/"), values("export f/attendance.csv "));
        assertEquals(List.of("f/"), values("export grp/T09 "));
        assertEquals(List.of("T09", "T10", "TG01"), values("export f/report.csv grp/t"));
        assertTrue(values("export f/\"reports/grp/T09 f/report.csv\" ").contains("grp/"));
        assertTrue(values("export f/\"reports gr").isEmpty());
        assertTrue(values("export f/\"reports grp/T").isEmpty());
        assertTrue(values("export f/attendance.csv").isEmpty());
        assertTrue(values("export f/report.csv grp/T09 ").isEmpty());
        assertTrue(values("undo ").isEmpty());
    }

    @Test
    public void suggest_newFieldsAtCaret_preservesOtherArgumentsAndQuotedPaths() {
        String export = "export f/\"reports/grp/T09 attendance.csv\" gr";
        assertEquals("export f/\"reports/grp/T09 attendance.csv\" grp/",
                FieldCompletion.suggest(export, export.length(), groups).getFirst().apply(export));
        String mark = "mark al w/1 s/absent";
        assertEquals("mark all w/1 s/absent",
                FieldCompletion.suggest(mark, "mark al".length(), groups).getFirst().apply(mark));
        String find = "find gr n/John";
        assertEquals("find grp/ n/John",
                FieldCompletion.suggest(find, "find gr".length(), groups).getFirst().apply(find));
    }

    @Test
    public void suggest_atCaret_replacesWholeFragmentAndPreservesSurroundingInput() {
        String text = "session grp/T99 w/3 d/2026-10-19";
        Suggestion selected = FieldCompletion.suggest(text, text.indexOf("T99") + 1, groups).getFirst();
        assertEquals("session grp/T09 w/3 d/2026-10-19", selected.apply(text));
        assertEquals(text.indexOf("T99"), selected.start());
        assertEquals(text.indexOf("T99") + 3, selected.end());
        String prefix = "session gr w/3 d/2026-10-19";
        assertEquals("session grp/ w/3 d/2026-10-19",
                FieldCompletion.suggest(prefix, prefix.indexOf("gr") + 2, groups).getFirst().apply(prefix));
        assertTrue(FieldCompletion.suggest(text, text.indexOf("grp/") + 1, groups).isEmpty());
        assertTrue(FieldCompletion.suggest(text, 3, groups).isEmpty());
    }

    @Test
    public void suggest_caretOutsideText_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> FieldCompletion.suggest("list", -1, groups));
        assertThrows(IllegalArgumentException.class, () -> FieldCompletion.suggest("list", 5, groups));
    }

    @Test
    public void suggest_sessionManagement_commandsFieldsAndGroups() {
        assertEquals(List.of("editsession"), values("edits"));
        assertEquals(List.of("deletesession"), values("deletes"));
        assertEquals(List.of("grp/", "w/", "d/"), values("editsession "));
        assertEquals(List.of("grp/", "w/"), values("deletesession "));
        assertEquals(List.of("T09", "T10", "TG01"), values("deletesession grp/t"));
        assertEquals(List.of("d/"), values("editsession grp/T09 w/1 "));
        assertTrue(values("deletesession grp/T09 w/1 ").isEmpty());
    }

    @Test
    public void suggestion_invalidRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Suggestion(-1, 2, "list"));
        assertThrows(IllegalArgumentException.class, () -> new Suggestion(3, 2, "list"));
        assertThrows(IllegalArgumentException.class, () -> new Suggestion(0, 5, "list").apply("li"));
    }

    private List<String> values(String text) {
        return FieldCompletion.suggest(text, text.length(), groups).stream().map(Suggestion::value).toList();
    }
}
