package seedu.address.logic.parser;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Supplies field completions without executing or validating commands. */
public final class FieldCompletion {
    private static final List<String> COMMANDS = List.of("add", "clear", "delete", "edit", "exit", "find",
            "help", "init", "list", "mark", "session");

    private FieldCompletion() {}

    /** Returns replacements for the token at the caret, preserving all other input. */
    public static List<Suggestion> suggest(String text, int caret, Collection<String> groups) {
        int commandStart = text.length() - text.stripLeading().length();
        int commandEnd = text.indexOf(' ', commandStart);
        if (commandEnd < 0) {
            commandEnd = text.length();
        }
        if (caret <= commandEnd) {
            if (caret <= commandStart) {
                return List.of();
            }
            String fragment = text.substring(commandStart, caret).toLowerCase(Locale.ROOT);
            String current = text.substring(commandStart, commandEnd);
            int end = commandEnd;
            return COMMANDS.stream().filter(command -> command.startsWith(fragment) && !command.equals(current))
                    .map(command -> new Suggestion(commandStart, end, command)).toList();
        }
        String command = text.substring(commandStart, commandEnd);
        List<String> prefixes = switch (command) {
            case "add", "edit" -> List.of("n/", "m/", "grp/", "e/", "p/", "a/", "t/");
            case "init", "list" -> List.of("grp/");
            case "session" -> List.of("grp/", "w/", "d/");
            case "mark" -> List.of("w/", "s/");
            case "find" -> List.of("n/");
            default -> List.of();
        };
        int start = caret;
        int end = caret;
        while (start > commandEnd && text.charAt(start - 1) != ' ') {
            start--;
        }
        while (end < text.length() && text.charAt(end) != ' ') {
            end++;
        }
        String token = text.substring(start, end);
        int slash = token.indexOf('/');
        Stream<String> candidates;
        if (slash < 0) {
            String otherFields = text.substring(0, start) + text.substring(end);
            candidates = prefixes.stream().filter(prefix -> prefix.equals("t/")
                    || !otherFields.contains(" " + prefix));
        } else {
            String prefix = token.substring(0, slash + 1);
            if (!prefixes.contains(prefix) || caret < start + slash + 1) {
                return List.of();
            }
            start += slash + 1;
            candidates = switch (prefix) {
                case "grp/" -> command.equals("init") ? Stream.empty() : groups.stream().sorted();
                case "s/" -> Stream.of("present", "absent");
                default -> Stream.empty();
            };
        }
        int replaceStart = start;
        int replaceEnd = end;
        String fragment = text.substring(start, caret).toLowerCase(Locale.ROOT);
        String current = text.substring(start, end);
        return candidates.distinct().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(fragment))
                .filter(value -> !value.equals(current))
                .map(value -> new Suggestion(replaceStart, replaceEnd, value)).toList();
    }

    /** A replacement range in the original input, with an exclusive end position. */
    public record Suggestion(int start, int end, String value) {
        /** Applies this suggestion without changing surrounding text. */
        public String apply(String text) {
            return text.substring(0, start) + value + text.substring(end);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
