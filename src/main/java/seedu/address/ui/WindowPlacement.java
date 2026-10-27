package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import javafx.geometry.Rectangle2D;

/**
 * Decides whether a saved window position can still be used on the screens that are connected now.
 */
public final class WindowPlacement {

    /** Height of the strip at the top of the window that holds the title bar, which is where it is dragged. */
    static final double TITLE_BAR_HEIGHT = 30;

    /** Width of the title bar that must be on a screen for the user to grab it and drag the window back. */
    static final double MIN_VISIBLE_TITLE_BAR_WIDTH = 100;

    private WindowPlacement() {
        // utility class, not meant to be instantiated
    }

    /**
     * Returns true if the title bar of a window with {@code bounds} would be on one of {@code screens} enough to
     * be dragged. A window saved on a screen that is no longer connected fails this check.
     */
    public static boolean isReachable(Rectangle2D bounds, List<Rectangle2D> screens) {
        requireAllNonNull(bounds, screens);
        double titleBarWidth = Math.min(bounds.getWidth(), MIN_VISIBLE_TITLE_BAR_WIDTH);
        for (Rectangle2D screen : screens) {
            double visibleWidth = Math.min(bounds.getMaxX(), screen.getMaxX())
                    - Math.max(bounds.getMinX(), screen.getMinX());
            // The top of the window must be on the screen, or the title bar is above or below it
            boolean topOnScreen = bounds.getMinY() >= screen.getMinY()
                    && bounds.getMinY() + TITLE_BAR_HEIGHT <= screen.getMaxY();
            if (topOnScreen && visibleWidth >= titleBarWidth) {
                return true;
            }
        }
        return false;
    }
}
