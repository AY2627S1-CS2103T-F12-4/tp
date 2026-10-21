package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Person;

public class CommentCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new CommentCommand(null, new Comment("attentive"), false));
        assertThrows(NullPointerException.class, () ->
                new CommentCommand(INDEX_FIRST_PERSON, null, false));
    }

    @Test
    public void execute_appendToEmptyComment_setsComment() {
        Person target = model.getFilteredPersonList().get(0);
        assertTrue(target.getComment().isEmpty());

        Comment addition = new Comment("attentive");
        Person expectedPerson = target.withComment(addition);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);

        assertCommandSuccess(new CommentCommand(INDEX_FIRST_PERSON, addition, false), model,
                String.format(CommentCommand.MESSAGE_APPEND_SUCCESS, target.getName().fullName, addition),
                expectedModel);
    }

    @Test
    public void execute_appendToExistingComment_joinsWithSeparator() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        model.setPerson(target, target.withComment(new Comment("attentive")));

        Comment addition = new Comment("asks good questions");
        Comment expectedComment = new Comment("attentive; asks good questions");

        new CommentCommand(INDEX_FIRST_PERSON, addition, false).execute(model);

        assertEquals(expectedComment, model.getFilteredPersonList().get(0).getComment());
    }

    @Test
    public void execute_replace_overwritesComment() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        model.setPerson(target, target.withComment(new Comment("attentive")));

        Comment replacement = new Comment("starts strong");
        new CommentCommand(INDEX_FIRST_PERSON, replacement, true).execute(model);

        assertEquals(replacement, model.getFilteredPersonList().get(0).getComment());
    }

    @Test
    public void execute_clear_removesComment() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        model.setPerson(target, target.withComment(new Comment("attentive")));

        new CommentCommand(INDEX_FIRST_PERSON, Comment.EMPTY, true).execute(model);

        assertTrue(model.getFilteredPersonList().get(0).getComment().isEmpty());
    }

    @Test
    public void execute_clearWhenAlreadyEmpty_throwsCommandException() {
        Person target = model.getFilteredPersonList().get(0);
        assertTrue(target.getComment().isEmpty());

        assertCommandFailure(new CommentCommand(INDEX_FIRST_PERSON, Comment.EMPTY, true), model,
                String.format(CommentCommand.MESSAGE_ALREADY_EMPTY, target.getName().fullName));
    }

    @Test
    public void execute_appendWouldExceedLimit_throwsCommandException() {
        Person target = model.getFilteredPersonList().get(0);
        Comment existing = new Comment("a".repeat(Comment.MAX_LENGTH - 1));
        model.setPerson(target, target.withComment(existing));

        Comment addition = new Comment("bb");
        int expectedLength = existing.lengthAfterAppending(addition);

        assertCommandFailure(new CommentCommand(INDEX_FIRST_PERSON, addition, false), model,
                String.format(Comment.MESSAGE_TOO_LONG, expectedLength));
    }

    @Test
    public void execute_replaceIsNotBlockedByTheLengthOfTheExistingComment() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        model.setPerson(target, target.withComment(new Comment("a".repeat(Comment.MAX_LENGTH))));

        Comment replacement = new Comment("short");
        new CommentCommand(INDEX_FIRST_PERSON, replacement, true).execute(model);

        assertEquals(replacement, model.getFilteredPersonList().get(0).getComment());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new CommentCommand(outOfBounds, new Comment("attentive"), false), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_leavesTheDisplayedListUnchanged() throws Exception {
        int sizeBefore = model.getFilteredPersonList().size();
        Person first = model.getFilteredPersonList().get(0);

        new CommentCommand(INDEX_FIRST_PERSON, new Comment("attentive"), false).execute(model);

        assertEquals(sizeBefore, model.getFilteredPersonList().size());
        assertEquals(first.getMatric(), model.getFilteredPersonList().get(0).getMatric());
    }

    @Test
    public void equals() {
        Comment attentive = new Comment("attentive");
        CommentCommand command = new CommentCommand(INDEX_FIRST_PERSON, attentive, false);

        // same values -> returns true
        assertTrue(command.equals(new CommentCommand(INDEX_FIRST_PERSON, attentive, false)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different type -> returns false
        assertFalse(command.equals(5));

        // different index -> returns false
        assertFalse(command.equals(new CommentCommand(INDEX_SECOND_PERSON, attentive, false)));

        // different comment -> returns false
        assertFalse(command.equals(new CommentCommand(INDEX_FIRST_PERSON, new Comment("distracted"), false)));

        // appending versus replacing -> returns false
        assertFalse(command.equals(new CommentCommand(INDEX_FIRST_PERSON, attentive, true)));
    }

    @Test
    public void toStringMethod() {
        Comment attentive = new Comment("attentive");
        CommentCommand command = new CommentCommand(INDEX_FIRST_PERSON, attentive, false);
        String expected = CommentCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + ", comment=" + attentive + ", isReplacing=false}";
        assertEquals(expected, command.toString());
    }
}
