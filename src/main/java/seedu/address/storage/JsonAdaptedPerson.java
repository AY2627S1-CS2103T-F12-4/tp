package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Comment;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String email;
    private final String matric;
    private final String group;
    private final String comment;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedAttendance> attendances = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("email") String email,
            @JsonProperty("matric") String matric, @JsonProperty("group") String group,
            @JsonProperty("comment") String comment,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("attendances") List<JsonAdaptedAttendance> attendances) {
        this.name = name;
        this.email = email;
        this.matric = matric;
        this.group = group;
        this.comment = comment;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (attendances != null) {
            this.attendances.addAll(attendances);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        email = source.getEmail().value;
        matric = source.getMatric().value;
        group = source.getGroup().value;
        comment = source.getComment().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        attendances.addAll(source.getAttendances().stream()
                .map(JsonAdaptedAttendance::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (tags.contains(null) || attendances.contains(null)) {
            throw new IllegalValueException("Person tag and attendance records must not be null.");
        }
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (matric == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Matric.class.getSimpleName()));
        }
        if (!Matric.isValidMatric(matric)) {
            throw new IllegalValueException(Matric.MESSAGE_CONSTRAINTS);
        }
        final Matric modelMatric = new Matric(matric);

        if (group == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Group.class.getSimpleName()));
        }
        if (!Group.isValidGroup(group)) {
            throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
        }
        final Group modelGroup = new Group(group);

        // An absent comment is written as null or omitted entirely, and reads back as the empty comment.
        final Comment modelComment;
        if (comment == null || Comment.normalize(comment).isEmpty()) {
            modelComment = Comment.EMPTY;
        } else if (!Comment.isValidComment(comment)) {
            throw new IllegalValueException(Comment.MESSAGE_CONSTRAINTS);
        } else {
            modelComment = new Comment(comment);
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);

        final Set<Attendance> modelAttendances = new HashSet<>();
        for (JsonAdaptedAttendance attendance : attendances) {
            modelAttendances.add(attendance.toModelType());
        }
        if (!Person.areValidAttendances(modelGroup, modelAttendances)) {
            throw new IllegalValueException(Person.MESSAGE_INVALID_ATTENDANCES);
        }

        return new Person(modelName, modelEmail, modelMatric, modelGroup, modelTags,
                modelAttendances, modelComment);
    }

}
