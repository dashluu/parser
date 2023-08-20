package types;

public class BoolType extends TypeInfo {
    public static final String ID = "Bool";
    public static final int SIZE = 1;
    private static final BoolType inst = new BoolType();

    public BoolType() {
        super(ID, true, SIZE);
    }

    /**
     * Gets the only instance of type boolean.
     *
     * @return a BoolType object.
     */
    public static BoolType getInst() {
        return inst;
    }
}
