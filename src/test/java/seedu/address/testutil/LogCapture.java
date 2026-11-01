package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import seedu.address.commons.core.LogsCenter;

/** Captures application logs at every level, restoring the original destinations when closed. */
public class LogCapture implements AutoCloseable {
    private final Logger logger;
    private final Level previousLevel;
    private final Handler[] previousHandlers;
    private final boolean hadParentHandlers;
    private final List<String> messages = new CopyOnWriteArrayList<>();
    private final Handler capture = new Handler() {
        private final SimpleFormatter formatter = new SimpleFormatter();

        @Override
        public void publish(LogRecord record) {
            messages.add(formatter.format(record));
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    };

    /** Captures even debug messages without writing synthetic student data to the normal log files. */
    public LogCapture() {
        LogsCenter.getLogger(LogCapture.class);
        logger = Logger.getLogger("ab3");
        previousLevel = logger.getLevel();
        previousHandlers = logger.getHandlers();
        hadParentHandlers = logger.getUseParentHandlers();
        for (Handler handler : previousHandlers) {
            logger.removeHandler(handler);
        }
        capture.setLevel(Level.ALL);
        logger.addHandler(capture);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    public String getOutput() {
        return String.join("\n", messages);
    }

    /** Checks formatted output, including exception details, without depending on log wording. */
    public void assertDoesNotContain(String... sensitiveValues) {
        String output = getOutput().toLowerCase(Locale.ROOT);
        for (String value : sensitiveValues) {
            assertFalse(output.contains(value.toLowerCase(Locale.ROOT)), "Logged private value: " + value);
        }
    }

    @Override
    public void close() {
        logger.removeHandler(capture);
        logger.setLevel(previousLevel);
        logger.setUseParentHandlers(hadParentHandlers);
        for (Handler handler : previousHandlers) {
            logger.addHandler(handler);
        }
    }
}
