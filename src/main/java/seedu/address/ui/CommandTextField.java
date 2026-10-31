package seedu.address.ui;

import javafx.scene.control.TextField;

/**
 * Preserves word boundaries before a single-line field filters pasted separators.
 */
public class CommandTextField extends TextField {
    @Override
    public void replaceText(int start, int end, String text) {
        super.replaceText(start, end, text.replaceAll("\\R|\\t", " "));
    }
}
