package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Email("alexyeoh@example.com"),
                                new Matric("A0287401J"), new Group("T09"),
                getTagSet("classrep")),
            new Person(new Name("Bernice Yu"), new Email("berniceyu@example.com"),
                                new Matric("A0287402K"), new Group("T09"),
                getTagSet("exchange")),
            new Person(new Name("Charlotte Oliveiro"), new Email("charlotte@example.com"),
                                new Matric("A0287403L"), new Group("T09"),
                getTagSet()),
            new Person(new Name("David Li"), new Email("lidavid@example.com"),
                                new Matric("A0287404M"), new Group("T09"),
                getTagSet("repeat")),
            new Person(new Name("Irfan Ibrahim"), new Email("irfan@example.com"),
                                new Matric("A0287405N"), new Group("T09"),
                getTagSet("exchange", "lateenrol")),
            new Person(new Name("Roy Balakrishnan"), new Email("royb@example.com"),
                                new Matric("A0287406P"), new Group("T09"),
                getTagSet())
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            if (!sampleAb.hasGroup(samplePerson.getGroup())) {
                sampleAb.addGroup(samplePerson.getGroup());
            }
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
