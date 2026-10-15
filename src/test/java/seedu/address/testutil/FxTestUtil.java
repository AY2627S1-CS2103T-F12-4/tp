package seedu.address.testutil;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs UI assertions on the JavaFX application thread, propagating failures to JUnit.
 */
public final class FxTestUtil {
    private static boolean started;

    private FxTestUtil() {}

    /**
     * Starts the toolkit once for the test JVM and keeps it alive between test windows.
     */
    private static synchronized void startToolkit() throws InterruptedException {
        if (started) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        if (!ready.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX toolkit did not start");
        }
        started = true;
    }

    /**
     * Executes a task on JavaFX's thread and waits for its result without arbitrary sleeps.
     */
    public static <T> T onFxThread(Callable<T> action) throws Exception {
        startToolkit();
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }
}
