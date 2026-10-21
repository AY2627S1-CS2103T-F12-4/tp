package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_GROUP_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_MATRIC_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.MATRIC_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.MATRIC_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GROUP_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_MATRIC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MATRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + TAG_DESC_FRIEND,
                new AddCommand(expectedPerson));


        // multiple tags - all accepted
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + EMAIL_DESC_BOB + MATRIC_DESC_BOB
                        + GROUP_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + TAG_DESC_FRIEND;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));


        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));


        // multiple matriculation numbers
        assertParseFailure(parser, MATRIC_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MATRIC));

        // multiple groups
        assertParseFailure(parser, GROUP_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + MATRIC_DESC_AMY + GROUP_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_EMAIL,
                        PREFIX_MATRIC, PREFIX_GROUP));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));



        // invalid matriculation number
        assertParseFailure(parser, INVALID_MATRIC_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MATRIC));

        // invalid group
        assertParseFailure(parser, INVALID_GROUP_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));



        // invalid matriculation number
        assertParseFailure(parser, validExpectedPersonString + INVALID_MATRIC_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MATRIC));

        // invalid group
        assertParseFailure(parser, validExpectedPersonString + INVALID_GROUP_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        assertParseSuccess(parser, NAME_DESC_AMY + EMAIL_DESC_AMY
                + MATRIC_DESC_AMY + GROUP_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_supportedNamesAndWhitespace_success() {
        for (String name : new String[]{"O'Neil", "Anne-Marie", "A", "A".repeat(70)}) {
            Person expectedPerson = new PersonBuilder(BOB).withName(name).withTags().build();
            assertParseSuccess(parser, " n/" + name + EMAIL_DESC_BOB
                    + MATRIC_DESC_BOB + GROUP_DESC_BOB, new AddCommand(expectedPerson));
        }

        Person expectedPerson = new PersonBuilder(BOB).withName("Alex Tan").withTags().build();
        assertParseSuccess(parser, " n/  Alex \t\u2003 Tan  " + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_invalidNames_failure() {
        for (String name : new String[]{"123", "Alex2", "'Alex", "Alex-", "A".repeat(71)}) {
            assertParseFailure(parser, " n/" + name + EMAIL_DESC_BOB
                    + MATRIC_DESC_BOB + GROUP_DESC_BOB, Name.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unknownParameter_failure() {
        String validArgs = NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB;
        assertParseFailure(parser, " x/ignored" + validArgs, AddCommand.MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, validArgs + " x/ignored", AddCommand.MESSAGE_UNKNOWN_PARAMETER);

        for (String prefix : new String[]{"x/", "extra/", "x1/", "bad_param/", "N/"}) {
            assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                    + " " + prefix + "ignored" + MATRIC_DESC_BOB + GROUP_DESC_BOB,
                    AddCommand.MESSAGE_UNKNOWN_PARAMETER);
        }
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB, expectedMessage);


        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_EMAIL_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB, expectedMessage);


        // missing matriculation number prefix
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                + VALID_MATRIC_BOB + GROUP_DESC_BOB, expectedMessage);

        // missing group prefix
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + VALID_GROUP_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_EMAIL_BOB
                + VALID_MATRIC_BOB + VALID_GROUP_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);


        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_EMAIL_DESC
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Email.MESSAGE_CONSTRAINTS);


        // invalid matriculation number
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_MATRIC_DESC + GROUP_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Matric.MESSAGE_CONSTRAINTS);

        // invalid group
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + INVALID_GROUP_DESC + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Group.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + INVALID_TAG_DESC + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + EMAIL_DESC_BOB
                + MATRIC_DESC_BOB + GROUP_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
