package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Stores a person's optional remark.
 */
public class Remark {

    public final String value;

    /**
     * Creates a remark. Empty text is allowed.
     */
    public Remark(String value) {
        requireNonNull(value);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Remark otherRemark)) {
            return false;
        }
        return value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
