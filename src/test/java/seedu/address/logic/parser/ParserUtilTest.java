package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.attendance.Status;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.session.Week;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_MATRIC = "B0123456X";
    private static final String INVALID_GROUP = "T9";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_MATRIC = "A0123456X";
    private static final String VALID_GROUP = "T09";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_repeatedWhitespace_returnsNormalizedName() throws Exception {
        assertEquals(new Name("Alex Tan"), ParserUtil.parseName("\u00a0Alex \t\n Tan\u2003"));
        assertEquals(new Name("O'Neil"), ParserUtil.parseName(" O'Neil "));
        assertEquals(new Name("Anne-Marie"), ParserUtil.parseName("Anne-Marie"));
    }

    @Test
    public void parseMatric_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseMatric((String) null));
    }

    @Test
    public void parseMatric_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseMatric(INVALID_MATRIC));
        assertThrows(ParseException.class, () -> ParserUtil.parseMatric("A012345X"));
    }

    @Test
    public void parseMatric_validValueWithoutWhitespace_returnsMatric() throws Exception {
        Matric expectedMatric = new Matric(VALID_MATRIC);
        assertEquals(expectedMatric, ParserUtil.parseMatric(VALID_MATRIC));
    }

    @Test
    public void parseMatric_validValueWithWhitespace_returnsTrimmedMatric() throws Exception {
        String matricWithWhitespace = WHITESPACE + VALID_MATRIC + WHITESPACE;
        Matric expectedMatric = new Matric(VALID_MATRIC);
        assertEquals(expectedMatric, ParserUtil.parseMatric(matricWithWhitespace));
    }

    @Test
    public void parseMatric_lowercaseValue_returnsUppercasedMatric() throws Exception {
        Matric expectedMatric = new Matric(VALID_MATRIC);
        assertEquals(expectedMatric, ParserUtil.parseMatric("a0123456x"));
        assertEquals(expectedMatric, ParserUtil.parseMatric("A0123456x"));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseEmail_length_acceptsUpToMaxLength() throws Exception {
        String domain = "@example.com";
        String longestEmail = "a".repeat(Email.MAX_LENGTH - domain.length()) + domain;

        assertEquals(new Email(longestEmail), ParserUtil.parseEmail(longestEmail)); // exactly the maximum length
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail("a" + longestEmail)); // one character over
    }

    @Test
    public void parseMatric_lowerCaseValueWithWhitespace_returnsUpperCaseTrimmedMatric() throws Exception {
        String matricWithWhitespace = WHITESPACE + VALID_MATRIC.toLowerCase() + WHITESPACE;
        assertEquals(new Matric(VALID_MATRIC), ParserUtil.parseMatric(matricWithWhitespace));
    }

    @Test
    public void parseWeek_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseWeek((String) null));
    }

    @Test
    public void parseWeek_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Week.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseWeek("14"));
    }

    @Test
    public void parseWeek_validValueWithWhitespace_returnsWeek() throws Exception {
        assertEquals(new Week(5), ParserUtil.parseWeek(WHITESPACE + "5" + WHITESPACE));
    }

    @Test
    public void parseStatus_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStatus((String) null));
    }

    @Test
    public void parseStatus_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStatus("late"));
    }

    @Test
    public void parseStatus_validValueWithWhitespace_returnsStatus() throws Exception {
        assertEquals(Status.ABSENT, ParserUtil.parseStatus(WHITESPACE + "Absent" + WHITESPACE));
    }

    @Test
    public void parseGroup_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseGroup((String) null));
    }

    @Test
    public void parseGroup_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseGroup(INVALID_GROUP));
    }

    @Test
    public void parseGroup_validValueWithoutWhitespace_returnsGroup() throws Exception {
        assertEquals(new Group(VALID_GROUP), ParserUtil.parseGroup(VALID_GROUP));
    }

    @Test
    public void parseGroup_lowerCaseValueWithWhitespace_returnsUpperCaseTrimmedGroup() throws Exception {
        String groupWithWhitespace = WHITESPACE + VALID_GROUP.toLowerCase() + WHITESPACE;
        assertEquals(new Group(VALID_GROUP), ParserUtil.parseGroup(groupWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTag_length_acceptsUpToMaxLength() throws Exception {
        String longestTag = "a".repeat(Tag.MAX_LENGTH);

        assertEquals(new Tag(longestTag), ParserUtil.parseTag(longestTag)); // exactly the maximum length
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(longestTag + "a")); // one character over
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseTags_sameTagInDifferentCase_keepsFirstSpelling() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of("exchange", "Exchange", "EXCHANGE"));

        assertEquals(1, actualTagSet.size());
        assertEquals("exchange", actualTagSet.iterator().next().tagName);
    }

    @Test
    public void parseTags_maximumNumberOfTags_returnsTagSet() throws Exception {
        assertEquals(Tag.MAX_TAGS_PER_STUDENT, ParserUtil.parseTags(numberedTags(Tag.MAX_TAGS_PER_STUDENT)).size());
    }

    @Test
    public void parseTags_tooManyTags_throwsParseException() {
        assertThrows(ParseException.class, Tag.MESSAGE_TOO_MANY_TAGS, () ->
                ParserUtil.parseTags(numberedTags(Tag.MAX_TAGS_PER_STUDENT + 1)));
    }

    @Test
    public void parseTags_extraTagsDifferingOnlyInCase_countedOnce() throws Exception {
        List<String> tags = new ArrayList<>(numberedTags(Tag.MAX_TAGS_PER_STUDENT));
        tags.add("TAG1");

        assertEquals(Tag.MAX_TAGS_PER_STUDENT, ParserUtil.parseTags(tags).size());
    }

    private static List<String> numberedTags(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> "tag" + i).toList();
    }
}
