package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespace is removed, and internal whitespace is collapsed to one space.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        if (!Name.isValidName(name)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(name);
    }

    /**
     * Parses a {@code String matric} into a {@code Matric}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code matric} is invalid.
     */
    public static Matric parseMatric(String matric) throws ParseException {
        requireNonNull(matric);
        String normalizedMatric = matric.trim().toUpperCase(Locale.ROOT);
        if (!Matric.isValidMatric(normalizedMatric)) {
            throw new ParseException(Matric.MESSAGE_CONSTRAINTS);
        }
        return new Matric(normalizedMatric);
    }


    /**
     * Parses a {@code String comment} into a {@code Comment}.
     * Surrounding whitespace is removed and internal runs of whitespace are collapsed.
     *
     * @throws ParseException if the given {@code comment} is invalid.
     */
    public static Comment parseComment(String comment) throws ParseException {
        requireNonNull(comment);
        if (!Comment.isValidComment(comment)) {
            throw new ParseException(Comment.MESSAGE_CONSTRAINTS);
        }
        return new Comment(comment);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed, and the address is lowercased so that
     * the same address entered with different casing is stored and compared identically.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!Email.isValidEmail(normalizedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(normalizedEmail);
    }

    /**
     * Parses a {@code String status} into a {@code Status}, ignoring letter case.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code status} is invalid.
     */
    public static Status parseStatus(String status) throws ParseException {
        requireNonNull(status);
        String trimmedStatus = status.trim();
        if (!Status.isValidStatus(trimmedStatus)) {
            throw new ParseException(Status.MESSAGE_CONSTRAINTS);
        }
        return Status.fromString(trimmedStatus);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses and normalizes a tutorial group code.
     *
     * @throws ParseException if the given {@code group} is invalid.
     */
    public static Group parseGroup(String group) throws ParseException {
        requireNonNull(group);
        String normalizedGroup = group.trim().toUpperCase(Locale.ROOT);
        if (!Group.isValidGroup(normalizedGroup)) {
            throw new ParseException(Group.MESSAGE_CONSTRAINTS);
        }
        return new Group(normalizedGroup);
    }

    /**
     * Parses a teaching week from 1 to 13.
     */
    public static Week parseWeek(String week) throws ParseException {
        requireNonNull(week);
        String trimmedWeek = week.trim();
        if (!Week.isValidWeek(trimmedWeek)) {
            throw new ParseException(Week.MESSAGE_CONSTRAINTS);
        }
        return new Week(Integer.parseInt(trimmedWeek));
    }

    /**
     * Parses a session date in YYYY-MM-DD format.
     */
    public static SessionDate parseSessionDate(String date) throws ParseException {
        requireNonNull(date);
        String trimmedDate = date.trim();
        if (!SessionDate.isValidSessionDate(trimmedDate)) {
            throw new ParseException(SessionDate.MESSAGE_CONSTRAINTS);
        }
        return new SessionDate(trimmedDate);
    }
}
