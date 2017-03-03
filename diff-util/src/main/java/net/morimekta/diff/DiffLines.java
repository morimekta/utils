package net.morimekta.diff;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Make diff based on lines.
 */
public class DiffLines extends DiffBase {
    private final LinkedList<Change> changeList;

    public DiffLines(String text1, String text2) {
        this(text1, text2, DiffOptions.defaults());
    }
    public DiffLines(String text1, String text2, DiffOptions options) {
        super(options, getDeadline(options));

        this.changeList = makeLineDiff(text1, text2);
    }

    private LinkedList<Change> makeLineDiff(String text1, String text2) {
        LinkedList<Change> changes = new LinkedList<>();
        LinkedList<String> lines1 = new LinkedList<>();
        LinkedList<String> lines2 = new LinkedList<>();
        Collections.addAll(lines1, text1.split("\\r?\\n"));
        Collections.addAll(lines2, text2.split("\\r?\\n"));

        while (true) {
            if (lines1.isEmpty() || lines2.isEmpty()) {
                break;
            }
            String l1 = lines1.peekFirst();
            String l2 = lines2.peekFirst();

            if (l1.equals(l2)) {
                changes.add(new Change(Operation.EQUAL, lines1.pollFirst()));
                lines2.pollFirst();
                continue;
            }
            // a differing line.
            int next1 = lines1.indexOf(l2);
            int next2 = lines2.indexOf(l1);
            if (next1 == -1 && next2 >= 0) {
                // Added line.
                changes.add(new Change(Operation.INSERT, lines2.pollFirst()));
                continue;
            }
            if (next2 == -1 && next1 >= 0) {
                // Removed line.
                changes.add(new Change(Operation.DELETE, lines1.pollFirst()));
                continue;
            }
            if (next1 >= 0 && next2 >= 0) {
                // Moved lines. Make diff on the *furthest* diff. The one that
                // is close is most likely the next non-diff.
                if (next1 > next2) {
                    changes.add(new Change(Operation.DELETE, lines1.pollFirst()));
                } else {
                    changes.add(new Change(Operation.INSERT, lines2.pollFirst()));
                }
                continue;
            }

            // added AND removed, aka change.
            changes.add(new Change(Operation.DELETE, lines1.pollFirst()));
            changes.add(new Change(Operation.INSERT, lines2.pollFirst()));
        }

        while (!lines1.isEmpty()) {
            changes.add(new Change(Operation.DELETE, lines1.pollFirst()));
        }

        while (!lines2.isEmpty()) {
            changes.add(new Change(Operation.INSERT, lines2.pollFirst()));
        }

        // Sort diff lines so that and continuous change of insert and delete becomes:
        // -- all deleted
        // -- all inserts
        LinkedList<Change> result = new LinkedList<>();
        // keep inserts for after deleted.
        LinkedList<Change> inserts = new LinkedList<>();
        for (Change change : changes) {
            switch (change.operation) {
                case EQUAL:
                    result.addAll(inserts);
                    inserts.clear();
                    result.add(change);
                    break;
                case DELETE:
                    result.add(change);
                    break;
                case INSERT:
                    inserts.add(change);
                    break;
            }
        }
        result.addAll(inserts);

        return result;
    }

    /**
     * Get the list of changes per line. The lines does NOT
     * @return
     */
    @Override
    public LinkedList<Change> getChangeList() {
        return changeList;
    }
}
