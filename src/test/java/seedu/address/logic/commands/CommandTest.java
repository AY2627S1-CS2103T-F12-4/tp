package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command.HistoryAction;
import seedu.address.model.Model;

public class CommandTest {

    @Test
    public void getHistoryAction_dataChangingCommand_commitsByDefault() {
        Command command = new StubCommand(true);

        assertEquals(HistoryAction.COMMIT, command.getHistoryAction());
    }

    @Test
    public void getHistoryAction_readOnlyCommand_doesNotChangeHistory() {
        Command command = new StubCommand(false);

        assertEquals(HistoryAction.NONE, command.getHistoryAction());
    }

    private static class StubCommand extends Command {
        private final boolean requiresSave;

        StubCommand(boolean requiresSave) {
            this.requiresSave = requiresSave;
        }

        @Override
        public CommandResult execute(Model model) {
            return new CommandResult("done");
        }

        @Override
        public boolean requiresSave() {
            return requiresSave;
        }
    }
}
