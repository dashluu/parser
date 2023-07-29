package types;

public class VoidType extends TypeInfo {
    public static final String ID = "Void";
    public static final int SIZE = 4;
    private static final VoidType inst = new VoidType();

    public VoidType() {
        super(ID, SIZE);
    }

    /**
     * Gets the only instance of type void.
     *
     * @return a VoidType object.
     */
    public static VoidType getInst() {
        return inst;
    }
}
