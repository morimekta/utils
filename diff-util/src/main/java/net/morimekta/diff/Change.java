package net.morimekta.diff;

/**
 * Class representing one diff operation.
 */
public class Change {
    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    public Operation operation;
    /**
     * The text associated with this diff operation.
     */
    public String    text;

    /**
     * Constructor.  Initializes the diff with the provided values.
     * @param operation One of INSERT, DELETE or EQUAL.
     * @param text The text being applied.
     */
    public Change(Operation operation, String text) {
        // Construct a diff with the specified operation and text.
        this.operation = operation;
        this.text = text;
    }

    /**
     * Display a human-readable version of this DiffBase.
     * @return text version.
     */
    public String toString() {
        String prettyText = this.text.replace('\n', '\u00b6');
        return "DiffBase(" + this.operation + ",\"" + prettyText + "\")";
    }

    /**
     * Create a numeric hash value for a DiffBase.
     * This function is not used by DMP.
     * @return Hash value.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (operation == null) ? 0 : operation.hashCode();
        result += prime * ((text == null) ? 0 : text.hashCode());
        return result;
    }

    /**
     * Is this DiffBase equivalent to another DiffBase?
     * @param obj Another DiffBase to compare against.
     * @return true or false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Change)) {
            return false;
        }
        Change other = (Change) obj;
        if (operation != other.operation) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }
}
