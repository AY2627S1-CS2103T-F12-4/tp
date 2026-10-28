package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;

public class WindowPlacementTest {

    private static final Rectangle2D LAPTOP = new Rectangle2D(0, 0, 1920, 1040);
    private static final Rectangle2D MONITOR_ON_LEFT = new Rectangle2D(-2560, 0, 2560, 1400);

    @Test
    public void isReachable_windowOnScreen_returnsTrue() {
        assertTrue(WindowPlacement.isReachable(new Rectangle2D(100, 100, 740, 600), List.of(LAPTOP)));
        // only the title bar needs to be on the screen
        assertTrue(WindowPlacement.isReachable(new Rectangle2D(100, 800, 740, 600), List.of(LAPTOP)));
        // a window narrower than the minimum title bar width only needs to be fully across
        assertTrue(WindowPlacement.isReachable(new Rectangle2D(1850, 100, 60, 600), List.of(LAPTOP)));
    }

    @Test
    public void isReachable_windowOnOtherConnectedScreen_returnsTrue() {
        Rectangle2D onLeftMonitor = new Rectangle2D(-2000, 200, 740, 600);
        assertTrue(WindowPlacement.isReachable(onLeftMonitor, List.of(LAPTOP, MONITOR_ON_LEFT)));
    }

    @Test
    public void isReachable_windowOnDisconnectedScreen_returnsFalse() {
        Rectangle2D onLeftMonitor = new Rectangle2D(-2000, 200, 740, 600);
        assertFalse(WindowPlacement.isReachable(onLeftMonitor, List.of(LAPTOP)));
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(2500, 100, 740, 600), List.of(LAPTOP)));
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(100, 100, 740, 600), List.of()));
    }

    @Test
    public void isReachable_titleBarOutOfReach_returnsFalse() {
        // the title bar is above the top of the screen
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(100, -50, 740, 600), List.of(LAPTOP)));
        // the title bar is below the bottom of the screen
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(100, 1020, 740, 600), List.of(LAPTOP)));
        // too little of the title bar is on the screen to grab it
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(1870, 100, 740, 600), List.of(LAPTOP)));
        assertFalse(WindowPlacement.isReachable(new Rectangle2D(-690, 100, 740, 600), List.of(LAPTOP)));
    }

    @Test
    public void isReachable_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> WindowPlacement.isReachable(null, List.of(LAPTOP)));
        assertThrows(NullPointerException.class, () ->
                WindowPlacement.isReachable(new Rectangle2D(0, 0, 740, 600), null));
    }
}
