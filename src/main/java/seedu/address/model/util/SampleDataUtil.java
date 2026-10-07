package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Matric;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.session.Session;
import seedu.address.model.session.SessionDate;
import seedu.address.model.session.Week;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"), new Matric("A0234567B"), new Group("T09"),
                getTagSet("friends")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"), new Matric("A0245678C"), new Group("T09"),
                getTagSet("colleagues", "friends")),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"), new Matric("A0256789D"), new Group("T09"),
                getTagSet("neighbours")),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"), new Matric("A0267890E"),
                new Group("T10"), getTagSet("family")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"), new Matric("A0278901F"), new Group("T10"),
                getTagSet("classmates")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"), new Matric("A0289012G"), new Group("T10"),
                getTagSet("colleagues"))
        };
    }

    /**
     * Returns sample sessions for the first three teaching weeks of the sample groups, so that attendance can be
     * marked before any session has been created.
     */
    public static Session[] getSampleSessions() {
        return new Session[] {
            new Session(new Group("T09"), new Week(1), new SessionDate("2026-08-11")),
            new Session(new Group("T09"), new Week(2), new SessionDate("2026-08-18")),
            new Session(new Group("T09"), new Week(3), new SessionDate("2026-08-25")),
            new Session(new Group("T10"), new Week(1), new SessionDate("2026-08-13")),
            new Session(new Group("T10"), new Week(2), new SessionDate("2026-08-20")),
            new Session(new Group("T10"), new Week(3), new SessionDate("2026-08-27"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Session sampleSession : getSampleSessions()) {
            sampleAb.addSession(sampleSession);
        }
        for (Person samplePerson : getSamplePersons()) {
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
