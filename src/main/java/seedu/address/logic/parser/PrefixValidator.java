package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;

/** Checks supported field prefixes while keeping relationship abbreviations inside names. */
final class PrefixValidator {
    private static final Pattern PREFIX_PATTERN =
            Pattern.compile("(?<!\\S)([\\p{L}][\\p{L}\\p{N}_-]*/)", Pattern.UNICODE_CHARACTER_CLASS);

    private PrefixValidator() {}

    /** Rejects unexpected prefixes using the command's existing feedback. */
    static void verifySupportedPrefixes(String args, Set<String> supportedPrefixes, String message)
            throws ParseException {
        requireAllNonNull(args, supportedPrefixes, message);
        if (findUnsupportedPrefix(args.replaceAll(Name.RELATIONSHIP_CONNECTOR_REGEX, ""), supportedPrefixes)
                .isPresent()) {
            throw new ParseException(message);
        }
    }

    /** Finds the first unexpected prefix without treating any field values as names. */
    static Optional<String> findUnsupportedPrefix(String args, Set<String> supportedPrefixes) {
        requireAllNonNull(args, supportedPrefixes);
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            if (!supportedPrefixes.contains(matcher.group())) {
                return Optional.of(matcher.group());
            }
        }
        return Optional.empty();
    }
}
