package parsers.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SyntaxBuff implements Iterable<SyntaxInfo> {
    // Delegation to the List interface
    private final List<SyntaxInfo> buff = new ArrayList<>();
    private int cursor;
    // A sentinel for the buffer so the buffer will not be underflowed
    private static final SyntaxInfo SENTINEL = new SyntaxInfo(null, SyntaxTag.END);

    public SyntaxBuff() {
        // Add the sentinel before pushing anything onto the stack
        buff.add(SENTINEL);
        cursor = 0;
    }

    /**
     * Adds a SyntaxInfo object to the buffer.
     *
     * @param syntaxInfo the SyntaxInfo object to be added.
     */
    public void add(SyntaxInfo syntaxInfo) {
        // Overwrite the sentinel with new element and add the sentinel to keep it at the tail
        buff.set(buff.size() - 1, syntaxInfo);
        buff.add(SENTINEL);
    }

    /**
     * Increments the cursor and returns the next SyntaxInfo object at the new cursor position.
     *
     * @return a valid SyntaxInfo object if it is within bounds, otherwise, return the sentinel.
     */
    public SyntaxInfo forward() {
        if (cursor == buff.size() - 1) {
            // The last one is always the sentinel
            return SENTINEL;
        }
        SyntaxInfo syntaxInfo = buff.get(cursor);
        ++cursor;
        return syntaxInfo;
    }

    /**
     * Decrements the cursor and returns the next SyntaxInfo object at the new cursor position.
     *
     * @return a valid SyntaxInfo object if it is within bounds, otherwise, return the sentinel.
     */
    public SyntaxInfo backward() {
        if (cursor == 0) {
            return SENTINEL;
        }
        SyntaxInfo syntaxInfo = buff.get(cursor);
        --cursor;
        return syntaxInfo;
    }

    /**
     * Peeks the SyntaxInfo object at the current cursor position.
     *
     * @return a SyntaxInfo object.
     */
    public SyntaxInfo peek() {
        return buff.get(cursor);
    }

    /**
     * Moves the cursor to the front of the buffer.
     */
    public void toFront() {
        cursor = 0;
    }

    /**
     * Checks if the cursor is at the front of the buffer.
     *
     * @return true if the cursor is at the front of the buffer and false otherwise.
     */
    public boolean atFront() {
        return cursor == 0;
    }

    /**
     * Moves the cursor to the back of the buffer.
     */
    public void toBack() {
        cursor = buff.size() - 1;
    }

    /**
     * Checks if the cursor is at the back of the buffer.
     *
     * @return true if the cursor is at the back of the buffer and false otherwise.
     */
    public boolean atBack() {
        return cursor == buff.size() - 1;
    }

    @Override
    public Iterator<SyntaxInfo> iterator() {
        return buff.iterator();
    }
}
