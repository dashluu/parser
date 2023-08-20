package types;

public class ObjTypeInfo extends TypeInfo {
    public static final int REF_SIZE = 4;

    public ObjTypeInfo(String id) {
        // Use reference size for an object's size field
        super(id, false, REF_SIZE);
    }
}
