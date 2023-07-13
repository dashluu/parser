package symbols;

public class LabelGen {
    private static int dataLabel = 0;
    private static int blockLabel = 0;

    public static int getDataLabel() {
        return dataLabel++;
    }

    public static int getBlockLabel() {
        return blockLabel++;
    }
}
