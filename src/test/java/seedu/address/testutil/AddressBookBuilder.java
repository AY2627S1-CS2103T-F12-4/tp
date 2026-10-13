package seedu.address.testutil;

import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;

/**
 * A utility class to help with building AddressBook objects.
 * Example usage: <br>
 *     {@code AddressBook ab = new AddressBookBuilder().withPerson("John", "Doe").build();}
 */
public class AddressBookBuilder {

    private AddressBook addressBook;

    public AddressBookBuilder() {
        addressBook = new AddressBook();
    }

    public AddressBookBuilder(AddressBook addressBook) {
        this.addressBook = addressBook;
    }

    /**
     * Adds a new {@code Person} to the {@code AddressBook} that we are building,
     * registering their tutorial group first if it is not registered yet.
     */
    public AddressBookBuilder withPerson(Person person) {
        if (!addressBook.hasGroup(person.getGroup())) {
            addressBook.addGroup(person.getGroup());
        }
        addressBook.addPerson(person);
        return this;
    }

    public AddressBook build() {
        return addressBook;
    }
}
