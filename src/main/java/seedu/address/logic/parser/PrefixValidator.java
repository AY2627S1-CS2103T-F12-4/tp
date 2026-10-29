package seedu.address.logic.parser;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

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
        Matcher matcher = PREFIX_PATTERN.matcher(args.replaceAll(Name.RELATIONSHIP_CONNECTOR_REGEX, ""));
        while (matcher.find()) {
            if (!supportedPrefixes.contains(matcher.group())) {
                throw new ParseException(message);
            }
        }
    }
}
