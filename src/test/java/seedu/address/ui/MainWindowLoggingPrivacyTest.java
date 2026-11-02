package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.onFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TextArea;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

public class MainWindowLoggingPrivacyTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void commandFeedback_andFailures_remainVisibleWithoutBeingLogged() throws Exception {
        Person student = new PersonBuilder().withName("Private UiStudent").withEmail("private.ui@example.com")
                .withMatric("A0777777X").build();
        AddressBook book = new AddressBookBuilder().withPerson(student).build();
        try (LogCapture logs = new LogCapture();
                MainWindowTestFixture fixture = MainWindowTestFixture.create(book, temporaryFolder)) {
            onFxThread(() -> {
                MainWindowTestFixture.enterCommand(fixture.stage(), "comment 1 c/PrivateUiHealthNote");
                TextArea feedback = (TextArea) fixture.stage().getScene().lookup("#resultDisplay");
                assertTrue(feedback.getText().contains("PrivateUiHealthNote"));

                MainWindowTestFixture.enterCommand(fixture.stage(),
                        "add n/Private UiStudent e/private.ui@example.com m/A0777777X grp/T09");
                assertTrue(feedback.getText().contains("A0777777X"));
                MainWindowTestFixture.enterCommand(fixture.stage(), "PrivateUiUnknown private.ui@example.com");
                assertTrue(MainWindowTestFixture.getCommandInput(fixture.stage())
                        .getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(1, fixture.model().getAddressBook().getPersonList().size());

                PersonListPanel emptyPanel = new PersonListPanel(FXCollections.observableArrayList(),
                        FXCollections.observableArrayList(), new SimpleObjectProperty<>());
                emptyPanel.scrollTo(student);
                return null;
            });
            logs.assertDoesNotContain("Private UiStudent", "private.ui@example.com", "A0777777X",
                    "PrivateUiHealthNote", "PrivateUiUnknown");
            assertTrue(logs.getOutput().contains("command"));
        }
    }
}
