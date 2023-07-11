package parsers.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SyntaxBuff implements Iterable<SyntaxInfo> {
    // Delegation to the List interface
    private final List<SyntaxInfo> buff = new ArrayList<>();
    private int cursor;
    private static final SyntaxInfo SENTINEL = new SyntaxInfo(null, SyntaxTag.END);

    public SyntaxBuff() {
        buff.add(SENTINEL);
        cursor = 0;
    }

    public void add(SyntaxInfo syntaxInfo) {
        // Overwrite the sentinel with new element and add the sentinel to keep it at the tail
        buff.set(buff.size() - 1, syntaxInfo);
        buff.add(SENTINEL);
        return;
    }

    public SyntaxInfo forward() {
        if (cursor == buff.size() - 1) {
            // The last one is always the sentinel
            return SENTINEL;
        }
        SyntaxInfo syntaxInfo = buff.get(cursor);
        ++cursor;
        return syntaxInfo;
    }

    public SyntaxInfo backward() {
        if (cursor == 0) {
            return SENTINEL;
        }
        SyntaxInfo syntaxInfo = buff.get(cursor);
        --cursor;
        return syntaxInfo;
    }

    public SyntaxInfo peek() {
        return buff.get(cursor);
    }

    public void toFront() {
        cursor = 0;
    }

    public boolean atFront() {
        return cursor == 0;
    }

    public void toBack() {
        cursor = buff.size() - 1;
    }

    public boolean atBack() {
        return cursor == buff.size() - 1;
    }

    @Override
    public Iterator<SyntaxInfo> iterator() {
        return buff.iterator();
    }
}
