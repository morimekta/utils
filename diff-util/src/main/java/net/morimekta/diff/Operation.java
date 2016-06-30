package net.morimekta.diff;

/**
 * The data structure representing a diff is a Linked list of DiffBase objects:
 * {Change(Operation.DELETE, "Hello"), Change(Operation.INSERT, "Goodbye"),
 *  Change(Operation.EQUAL, " world.")}
 * which means: delete "Hello", add "Goodbye" and keep " world."
 */
public enum Operation {
    DELETE, INSERT, EQUAL
}
