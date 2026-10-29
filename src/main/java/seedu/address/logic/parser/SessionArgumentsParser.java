package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the single-token fields shared by session editing and deletion. */
final class SessionArgumentsParser {
    private SessionArgumentsParser() {}

    /** Rejects unsupported parameters and extra text before parsing individual field values. */
    static ArgumentMultimap parse(String args, String usage, Prefix... requiredPrefixes) throws ParseException {
        requireAllNonNull(args, usage, requiredPrefixes);
        Set<String> supportedPrefixes = Stream.of(requiredPrefixes).map(Prefix::getPrefix).collect(Collectors.toSet());
        Optional<String> unsupportedPrefix = PrefixValidator.findUnsupportedPrefix(args, supportedPrefixes);
        if (unsupportedPrefix.isPresent()) {
            throw new ParseException("Unsupported parameter '" + unsupportedPrefix.orElseThrow()
                    + "'. Remove it.\n" + usage);
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, requiredPrefixes);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException("Unexpected text '" + arguments.getPreamble()
                    + "' before the parameters. Remove it.\n" + usage);
        }
        if (Stream.of(requiredPrefixes).anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        arguments.verifyNoDuplicatePrefixesFor(requiredPrefixes);
        for (Prefix prefix : requiredPrefixes) {
            verifySingleValue(arguments.getValue(prefix).orElseThrow(), prefix, usage);
        }
        return arguments;
    }

    private static void verifySingleValue(String value, Prefix prefix, String usage) throws ParseException {
        String[] parts = value.strip().split("(?U)\\s+", 2);
        if (parts.length > 1) {
            throw new ParseException("Unexpected text '" + parts[1] + "' after " + prefix
                    + ". Remove it.\n" + usage);
        }
    }
}
