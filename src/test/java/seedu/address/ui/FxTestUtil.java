package seedu.address.ui;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs test code on the JavaFX application thread, starting the JavaFX toolkit first if needed.
 */
public class FxTestUtil {

    private static final long TIMEOUT_SECONDS = 30;

    private static boolean isStarted;
    private static String startFailure;

    /**
     * Represents test code that may throw.
     */
    @FunctionalInterface
    public interface FxAction {
        void run() throws Exception;
    }

    private FxTestUtil() {} // prevents instantiation

    /**
     * Runs {@code action} on the JavaFX application thread and waits for it, rethrowing anything it throws.
     * Skips the calling test if the JavaFX toolkit cannot start, such as on a machine without a display.
     */
    public static void runOnFxThread(FxAction action) throws Exception {
        startToolkit();
        CountDownLatch done = new CountDownLatch(1);
        Exception[] failure = new Exception[1];
        Error[] error = new Error[1];
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                failure[0] = e;
            } catch (Error e) {
                error[0] = e;
            } finally {
                done.countDown();
            }
        });
        if (!done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX action did not finish within " + TIMEOUT_SECONDS + " seconds");
        }
        if (error[0] != null) {
            throw error[0];
        }
        if (failure[0] != null) {
            throw failure[0];
        }
    }

    private static synchronized void startToolkit() throws InterruptedException {
        if (!isStarted && startFailure == null) {
            // Software rendering needs no graphics card, so the tests also run on CI machines
            System.setProperty("prism.order", "sw");
            CountDownLatch started = new CountDownLatch(1);
            try {
                Platform.startup(started::countDown);
                started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                Platform.setImplicitExit(false);
                isStarted = true;
            } catch (IllegalStateException e) {
                // The toolkit was already started elsewhere in this test run
                isStarted = true;
            } catch (RuntimeException e) {
                startFailure = e.getMessage();
            }
        }
        assumeTrue(isStarted, "JavaFX cannot start here: " + startFailure);
    }
}
