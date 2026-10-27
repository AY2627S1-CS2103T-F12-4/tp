package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.KEYWORD_ALL;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Supplies field completions without executing or validating commands. */
public final class FieldCompletion {
    private static final List<String> COMMANDS = List.of("add", "clear", "comment", "delete", "edit", "exit",
            "export", "find", "help", "init", "list", "mark", "redo", "session", "undo");

    private FieldCompletion() {}

    /** Returns replacements for the token at the caret, preserving all other input. */
    public static List<Suggestion> suggest(String text, int caret, Collection<String> groups) {
        requireAllNonNull(text, groups);
        checkArgument(caret >= 0 && caret <= text.length(), "The caret must be within the text");
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
        String fields = command.equals("export") ? maskQuotedPath(text) : text;
        if (command.equals("export") && text.substring(0, caret).chars().filter(c -> c == '"').count() % 2 != 0) {
            return List.of();
        }
        List<String> prefixes = switch (command) {
            case "add", "edit" -> List.of("n/", "m/", "grp/", "e/", "t/");
            case "comment" -> List.of("c/", "c-/");
            case "export" -> List.of("f/", "grp/");
            case "init", "list" -> List.of("grp/");
            case "session" -> List.of("grp/", "w/", "d/");
            case "mark" -> List.of("w/", "s/");
            case "find" -> List.of("n/", "m/", "grp/");
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
            String otherFields = fields.substring(0, start) + fields.substring(end);
            candidates = prefixes.stream().filter(prefix -> prefix.equals("t/")
                    || !otherFields.contains(" " + prefix))
                    .filter(prefix -> !command.equals("comment")
                            || !(otherFields.contains(" c/") || otherFields.contains(" c-/")));
            if ((command.equals("mark") || command.equals("delete"))
                    && text.substring(commandEnd, start).isBlank()) {
                candidates = Stream.concat(Stream.of(KEYWORD_ALL), candidates);
            }
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

    /** Hides quoted CSV path contents so prefix-like directory names are not treated as fields. */
    private static String maskQuotedPath(String text) {
        StringBuilder masked = new StringBuilder(text);
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                quoted = !quoted;
            } else if (quoted) {
                masked.setCharAt(i, ' ');
            }
        }
        // Suggestions are positions in the original text, so masking must not move any character
        assert masked.length() == text.length() : "Masking a path should keep the text's length";
        return masked.toString();
    }

    /** A replacement range in the original input, with an exclusive end position. */
    public record Suggestion(int start, int end, String value) {
        /**
         * Creates a suggestion replacing the text from {@code start} up to, but not including, {@code end}.
         */
        public Suggestion {
            requireNonNull(value);
            checkArgument(start >= 0 && start <= end, "A suggestion must replace a valid range of the text");
        }

        /** Applies this suggestion without changing surrounding text. */
        public String apply(String text) {
            requireNonNull(text);
            checkArgument(end <= text.length(), "A suggestion must replace a range within the text");
            return text.substring(0, start) + value + text.substring(end);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
