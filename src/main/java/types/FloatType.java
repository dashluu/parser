package types;

public class FloatType extends TypeInfo {
    public static final String ID = "Float";
    public static final int SIZE = 4;
    private static final FloatType inst = new FloatType();

    private FloatType() {
        super(ID, SIZE);
    }

    /**
     * Gets the only instance of type floating-point.
     *
     * @return a FloatType object.
     */
    public static FloatType getInst() {
        return inst;
    }
}
