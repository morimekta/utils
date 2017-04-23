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

    public String fullDiff() {
        StringBuilder builder = new StringBuilder();
        for (Change ch : getChangeList()) {
            builder.append(ch.patchLine())
                   .append("\n");
        }
        return builder.toString();
    }

    public String patch() {
        StringBuilder builder = new StringBuilder();
        int src_pos = 1, trg_pos = 1;
        LinkedList<Change> changes = new LinkedList<>(getChangeList());
        while (!changes.isEmpty()) {
            if (changes.peekFirst().operation == Operation.EQUAL) {
                changes.pollFirst();
                ++src_pos;
                ++trg_pos;
                continue;
            }
            LinkedList<Change> upcoming = new LinkedList<>();

            int ins = 0;
            int rms = 0;
            while (!changes.isEmpty()) {
                if (changes.peekFirst().operation == Operation.EQUAL) {
                    break;
                }
                Change ch = changes.pollFirst();
                upcoming.add(ch);
                if (ch.operation == Operation.INSERT) {
                    ++ins;
                } else {
                    ++rms;
                }
            }

            builder.append("@@ -")
                   .append(src_pos)
                   .append(',')
                   .append(rms)
                   .append(" +")
                   .append(trg_pos)
                   .append(',')
                   .append(ins)
                   .append(" @@\n");

            for (Change ch : upcoming) {
                builder.append(ch.patchLine())
                       .append("\n");
                if (ch.operation == Operation.INSERT) {
                    ++trg_pos;
                } else {
                    ++src_pos;
                }
            }
        }

        return builder.toString();
    }

    private LinkedList<Change> makeLineDiff(String source, String target) {
        LinkedList<String> src_lines = new LinkedList<>();
        LinkedList<String> trg_lines = new LinkedList<>();
        Collections.addAll(src_lines, source.split("\\r?\\n"));
        Collections.addAll(trg_lines, target.split("\\r?\\n"));

        LinkedList<Change> beg = new LinkedList<>();
        LinkedList<Change> end = new LinkedList<>();
        while (true) {
            // This checks if the last change is a pure insert or delete.
            if (src_lines.isEmpty() || trg_lines.isEmpty()) {
                break;
            }

            // No change on all top lines -> beg (EQ).
            String src_first = src_lines.peekFirst();
            String trg_first = trg_lines.peekFirst();

            if (src_first.equals(trg_first)) {
                beg.add(new Change(Operation.EQUAL, src_lines.pollFirst()));
                trg_lines.pollFirst();
                continue;
            }

            // No change in bottom lines -> end (EQ)
            String src_last = src_lines.peekLast();
            String trg_last = trg_lines.peekLast();
            if (src_last.equals(trg_last)) {
                end.add(0, new Change(Operation.EQUAL, src_lines.pollLast()));
                trg_lines.pollLast();
                continue;
            }

            // a differing line.
            int up_next = src_lines.indexOf(trg_first);
            int down_next = trg_lines.indexOf(src_first);
            if (up_next == -1 && down_next >= 0) {
                // Added line.
                beg.add(new Change(Operation.INSERT, trg_lines.pollFirst()));
                continue;
            }
            if (down_next == -1 && up_next >= 0) {
                // Removed line.
                beg.add(new Change(Operation.DELETE, src_lines.pollFirst()));
                continue;
            }

            if (up_next >= 0 && down_next >= 0) {
                // Check number of lines moved **UP** (top in target found in source)
                int up_move = 1;
                while (up_next + up_move < src_lines.size() &&
                       up_move < trg_lines.size()) {
                    if (src_lines.get(up_next + up_move)
                              .equals(trg_lines.get(up_move))) {
                        ++up_move;
                    } else {
                        break;
                    }
                }

                // Check number of lines moved **DOWN** (top in source found in target)
                int down_move = 1;
                while (down_next + down_move < trg_lines.size() &&
                       down_move < src_lines.size()) {
                    if (trg_lines.get(down_next + down_move)
                              .equals(src_lines.get(down_move))) {
                        ++down_move;
                    } else {
                        break;
                    }
                }

                // First choose the shorter consecutive diff.
                if (up_move > down_move) {
                    up_move = 0;
                } else if (up_move < down_move) {
                    down_move = 0;
                } else {
                    // Then the closest diff.
                    if (up_next > down_next){
                        up_move = 0;
                    } else {
                        down_move = 0;
                    }
                }

                if (down_move > 0) {
                    while (down_move-- > 0) {
                        beg.add(new Change(Operation.DELETE, src_lines.pollFirst()));
                    }
                } else {
                    while (up_move-- > 0) {
                        beg.add(new Change(Operation.INSERT, trg_lines.pollFirst()));
                    }
                }
                continue;
            }

            // added AND removed, aka change.
            beg.add(new Change(Operation.DELETE, src_lines.pollFirst()));
            beg.add(new Change(Operation.INSERT, trg_lines.pollFirst()));
        }

        while (!src_lines.isEmpty()) {
            beg.add(new Change(Operation.DELETE, src_lines.pollFirst()));
        }
        while (!trg_lines.isEmpty()) {
            beg.add(new Change(Operation.INSERT, trg_lines.pollFirst()));
        }

        LinkedList<Change> changes = new LinkedList<>();
        changes.addAll(beg);
        changes.addAll(end);

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
