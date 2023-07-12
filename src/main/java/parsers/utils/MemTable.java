package parsers.utils;

import java.util.HashMap;

public class MemTable {
    private static long dataMem = 0;
    private static long blockMem = 0;
    private final HashMap<String, Long> memMap = new HashMap<>();
    private final MemTable parent;
    public static final int END = -1;

    public MemTable(MemTable parent) {
        this.parent = parent;
    }

    public static long nextDataMem() {
        return dataMem++;
    }

    public static long nextBlockMem() {
        return blockMem++;
    }

    public MemTable getParent() {
        return parent;
    }

    public void registerMem(String id, long mem) {
        memMap.put(id, mem);
    }

    public long getMem(String id) {
        MemTable table = this;
        long mem = END;
        while (table != null && mem == END) {
            mem = table.memMap.get(id);
            table = table.parent;
        }
        return mem;
    }
}
