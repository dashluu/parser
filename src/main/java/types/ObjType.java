package types;

public class ObjType extends TypeInfo {
    public static final int REF_SIZE = 4;

    public ObjType(String id) {
        // Use reference size for an object's size field
        super(id, false, REF_SIZE);
    }
}
