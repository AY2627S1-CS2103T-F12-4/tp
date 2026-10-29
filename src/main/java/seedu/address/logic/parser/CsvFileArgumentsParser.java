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
        Path path = parsePath(arguments.getValue(PREFIX_FILE).orElse(""));
        Group group = arguments.getValue(PREFIX_GROUP).isPresent()
                ? ParserUtil.parseGroup(arguments.getValue(PREFIX_GROUP).get()) : null;
        return new CsvFileArguments(path, group);
    }

    private Path parsePath(String value) throws ParseException {
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
        return path;
    }

    private ArgumentMultimap tokenize(String args) throws ParseException {
        List<PrefixPosition> positions = findPrefixPositions(maskQuotedValues(args));
        if (positions.isEmpty() || !args.substring(0, positions.getFirst().start()).isBlank()) {
            throw invalidFormat();
        }
        ArgumentMultimap result = new ArgumentMultimap();
        for (int i = 0; i < positions.size(); i++) {
            PrefixPosition position = positions.get(i);
            int end = i + 1 < positions.size() ? positions.get(i + 1).start() : args.length();
            result.put(position.prefix(), extractValue(args, position, end));
        }
        return result;
    }

    private String maskQuotedValues(String args) throws ParseException {
        StringBuilder searchable = new StringBuilder(args);
        boolean isInsideQuotes = false;
        for (int i = 0; i < args.length(); i++) {
            if (args.charAt(i) == '"') {
                isInsideQuotes = !isInsideQuotes;
                searchable.setCharAt(i, ' ');
            } else if (isInsideQuotes) {
                searchable.setCharAt(i, ' ');
            }
        }
        if (isInsideQuotes) {
            throw invalidFormat();
        }
        return searchable.toString();
    }

    private List<PrefixPosition> findPrefixPositions(String searchable) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(searchable);
        List<PrefixPosition> positions = new ArrayList<>();
        while (matcher.find()) {
            if (!matcher.group().equals(PREFIX_FILE.getPrefix())
                    && !matcher.group().equals(PREFIX_GROUP.getPrefix())) {
                throw invalidFormat();
            }
            positions.add(new PrefixPosition(new Prefix(matcher.group()), matcher.start()));
        }
        return positions;
    }

    private String extractValue(String args, PrefixPosition position, int end) throws ParseException {
        String value = args.substring(position.start() + position.prefix().getPrefix().length(), end).strip();
        if (value.contains("\"") && !(position.prefix().equals(PREFIX_FILE) && value.matches("\"[^\"]*\""))) {
            throw invalidFormat();
        }
        return value;
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }

    record CsvFileArguments(Path path, Group group) {
    }

    private record PrefixPosition(Prefix prefix, int start) {
    }
}
