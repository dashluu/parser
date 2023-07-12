package parsers.utils;

// Determines memory locations for data and blocks of instructions
public class MemSys {
    private static long dataMem = 1;
    private static long blockMem = 1;

    public static void updateDataMem() {
        ++dataMem;
    }

    public static long getDataMem() {
        return dataMem;
    }

    public static void updateBlockMem() {
        ++blockMem;
    }

    public static long getBlockMem() {
        return blockMem;
    }
}
