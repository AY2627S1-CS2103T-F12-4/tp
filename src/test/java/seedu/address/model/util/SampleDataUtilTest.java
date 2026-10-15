package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Group;

public class SampleDataUtilTest {
    @Test
    public void sampleGroups_areRegisteredOnceAndCanBeListed() throws Exception {
        ReadOnlyAddressBook sample = SampleDataUtil.getSampleAddressBook();
        assertEquals(SampleDataUtil.getSamplePersons().length, sample.getPersonList().size());
        assertEquals(sample.getPersonList().stream().map(p -> p.getGroup()).distinct().count(),
                (long) sample.getGroupList().size());
        assertTrue(sample.getPersonList().stream().allMatch(p -> sample.getGroupList().contains(p.getGroup())));
        ModelManager model = new ModelManager(sample, new UserPrefs());
        for (Group group : sample.getGroupList()) {
            new ListCommand(group).execute(model);
            assertEquals(sample.getPersonList().stream().filter(p -> p.getGroup().equals(group)).count(),
                    (long) model.getFilteredPersonList().size());
            assertTrue(model.getFilteredPersonList().stream().allMatch(p -> p.getGroup().equals(group)));
        }
    }
}
