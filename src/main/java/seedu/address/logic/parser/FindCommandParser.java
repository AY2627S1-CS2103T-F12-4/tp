package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MATRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.NameContainsQueryPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern PREFIX_PATTERN =
            Pattern.compile("(?<!\\S)([\\p{L}][\\p{L}\\p{N}_-]*/)");
    private static final Set<String> SUPPORTED_PREFIXES =
            Set.of(PREFIX_NAME.getPrefix(), PREFIX_MATRIC.getPrefix(), PREFIX_GROUP.getPrefix());

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        // The tokenizer recognizes prefixes preceded by a space.
        String normalizedArgs = " " + args.replaceAll("(?U)\\s+", " ").strip();
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(normalizedArgs);
        while (prefixMatcher.find()) {
            if (!SUPPORTED_PREFIXES.contains(prefixMatcher.group())) {
                throw new ParseException(FindCommand.MESSAGE_UNKNOWN_PARAMETER);
            }
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_NAME, PREFIX_MATRIC,
                PREFIX_GROUP);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_MATRIC, PREFIX_GROUP);

        boolean hasFilter = SUPPORTED_PREFIXES.stream()
                .anyMatch(prefix -> argMultimap.getValue(new Prefix(prefix)).isPresent());
        boolean hasBlankFilter = SUPPORTED_PREFIXES.stream()
                .anyMatch(prefix -> argMultimap.getValue(new Prefix(prefix)).filter(String::isEmpty).isPresent());
        if (!hasFilter || hasBlankFilter || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        NameContainsQueryPredicate namePredicate = null;
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            String query = argMultimap.getValue(PREFIX_NAME).get();
            if (!NameContainsQueryPredicate.isValidQuery(query)) {
                throw new ParseException(NameContainsQueryPredicate.MESSAGE_CONSTRAINTS);
            }
            namePredicate = new NameContainsQueryPredicate(query);
        }
        Matric matric = argMultimap.getValue(PREFIX_MATRIC).isPresent()
                ? ParserUtil.parseMatric(argMultimap.getValue(PREFIX_MATRIC).get()) : null;
        Group group = argMultimap.getValue(PREFIX_GROUP).isPresent()
                ? ParserUtil.parseGroup(argMultimap.getValue(PREFIX_GROUP).get()) : null;
        return new FindCommand(namePredicate, matric, group);
    }

}
