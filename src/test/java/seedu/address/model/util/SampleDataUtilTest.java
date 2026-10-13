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
        assertEquals(6, sample.getPersonList().size());
        assertEquals(2, sample.getGroupList().size());
        assertTrue(sample.getPersonList().stream().allMatch(p -> sample.getGroupList().contains(p.getGroup())));
        ModelManager model = new ModelManager(sample, new UserPrefs());
        for (Group group : sample.getGroupList()) {
            new ListCommand(group).execute(model);
            assertEquals(3, model.getFilteredPersonList().size());
            assertTrue(model.getFilteredPersonList().stream().allMatch(p -> p.getGroup().equals(group)));
        }
    }
}
