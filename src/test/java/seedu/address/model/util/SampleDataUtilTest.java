package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_registersGroupOfEverySamplePerson() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();

        assertEquals(SampleDataUtil.getSamplePersons().length, sampleAddressBook.getPersonList().size());
        for (Person person : sampleAddressBook.getPersonList()) {
            assertTrue(sampleAddressBook.getGroupList().contains(person.getGroup()));
        }
    }
}
