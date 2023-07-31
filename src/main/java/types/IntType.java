package types;

public class IntType extends TypeInfo {
    public static final String ID = "Int";
    public static final int SIZE = 4;
    private static final IntType inst = new IntType();

    private IntType() {
        super(ID, TypeInfoType.PRIMITIVE, SIZE);
    }

    /**
     * Gets the only instance of type integer.
     *
     * @return an IntType object.
     */
    public static IntType getInst() {
        return inst;
    }
}
