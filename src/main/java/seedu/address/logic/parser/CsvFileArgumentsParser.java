package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;
import seedu.address.storage.CsvFileAccess;

/**
 * Parses CSV command arguments, preserving path whitespace and quoted prefix-like fragments.
 */
class CsvFileArgumentsParser {
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([\\p{L}][\\p{L}\\p{N}_-]*/)",
            Pattern.UNICODE_CHARACTER_CLASS);

    private final String usage;

    CsvFileArgumentsParser(String usage) {
        this.usage = requireNonNull(usage);
    }

    CsvFileArguments parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = tokenize(args);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_FILE, PREFIX_GROUP);
        String value = arguments.getValue(PREFIX_FILE).orElse("");
        // Tokenization has already verified that any quotes surround the complete path.
        if (value.startsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.isBlank()) {
            throw invalidFormat();
        }
        Path path;
        try {
            path = Path.of(value);
        } catch (InvalidPathException e) {
            throw new ParseException(CsvFileAccess.MESSAGE_INVALID_PATH);
        }
        if (value.codePoints().anyMatch(Character::isISOControl)) {
            throw new ParseException(CsvFileAccess.MESSAGE_INVALID_PATH);
        }
        if (!CsvFileAccess.isCsvPath(path)) {
            throw new ParseException(CsvFileAccess.MESSAGE_INVALID_PATH);
        }
        Group group = arguments.getValue(PREFIX_GROUP).isPresent()
                ? ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).get()) : null;
        return new CsvFileArguments(path, group);
    }

    private ArgumentMultimap tokenize(String args) throws ParseException {
        StringBuilder searchable = new StringBuilder(args);
        boolean quoted = false;
        for (int i = 0; i < args.length(); i++) {
            if (args.charAt(i) == '"') {
                quoted = !quoted;
                searchable.setCharAt(i, ' ');
            } else if (quoted) {
                searchable.setCharAt(i, ' ');
            }
        }
        if (quoted) {
            throw invalidFormat();
        }
        Matcher matcher = PREFIX_PATTERN.matcher(searchable);
        List<Integer> starts = new ArrayList<>();
        List<Prefix> prefixes = new ArrayList<>();
        while (matcher.find()) {
            if (!matcher.group().equals(PREFIX_FILE.getPrefix())
                    && !matcher.group().equals(PREFIX_GROUP.getPrefix())) {
                throw invalidFormat();
            }
            starts.add(matcher.start());
            prefixes.add(new Prefix(matcher.group()));
        }
        if (starts.isEmpty() || !args.substring(0, starts.getFirst()).isBlank()) {
            throw invalidFormat();
        }
        ArgumentMultimap result = new ArgumentMultimap();
        for (int i = 0; i < starts.size(); i++) {
            int end = i + 1 < starts.size() ? starts.get(i + 1) : args.length();
            String value = args.substring(starts.get(i) + prefixes.get(i).getPrefix().length(), end).strip();
            if (value.contains("\"") && !(prefixes.get(i).equals(PREFIX_FILE)
                    && value.matches("\"[^\"]*\""))) {
                throw invalidFormat();
            }
            result.put(prefixes.get(i), value);
        }
        return result;
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }

    record CsvFileArguments(Path path, Group group) {
    }
}
