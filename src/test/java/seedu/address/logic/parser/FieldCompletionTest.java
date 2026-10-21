package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(List.of("p/"), values("edit 1 p"));
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
        assertEquals(List.of("edit", "exit"), values("e"));
        assertEquals(List.of("list"), values("LI"));
        assertEquals(List.of("session"), values("  ses"));
        assertTrue(values("nonsense").isEmpty());
        assertTrue(values("   ").isEmpty());
        String text = "li grp/T09";
        assertEquals("list grp/T09", FieldCompletion.suggest(text, 2, groups).getFirst().apply(text));
    }

    @Test
    public void suggest_values_matchesCaseInsensitivelyAndReadsCurrentGroups() {
        assertEquals(List.of("T09", "T10", "TG01"), values("list grp/t"));
        assertEquals(List.of("present", "absent"), values("mark 1 s/"));
        assertEquals(List.of("present"), values("mark 1 s/P"));
        assertTrue(values("list grp/T99").isEmpty());
        assertTrue(values("init grp/T").isEmpty());
        assertTrue(values("find grp/T").isEmpty());
        assertTrue(values("add n/Jo").isEmpty());
        assertTrue(values("mark 1 s/present").isEmpty());
        groups.add("T11");
        assertEquals(List.of("T10", "T11"), values("list grp/T1"));
        groups.clear();
        assertTrue(values("list grp/").isEmpty());
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

    private List<String> values(String text) {
        return FieldCompletion.suggest(text, text.length(), groups).stream().map(Suggestion::value).toList();
    }
}
