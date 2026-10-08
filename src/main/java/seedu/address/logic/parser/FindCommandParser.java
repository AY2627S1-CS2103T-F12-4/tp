package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsQueryPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([\\p{L}]+/)");

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
            if (!prefixMatcher.group().equals(PREFIX_NAME.getPrefix())) {
                throw new ParseException(FindCommand.MESSAGE_UNKNOWN_PARAMETER);
            }
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_NAME);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME);

        String query = argMultimap.getValue(PREFIX_NAME).orElse("");
        if (query.isEmpty() || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (!NameContainsQueryPredicate.isValidQuery(query)) {
            throw new ParseException(NameContainsQueryPredicate.MESSAGE_CONSTRAINTS);
        }
        return new FindCommand(new NameContainsQueryPredicate(query));
    }

}
