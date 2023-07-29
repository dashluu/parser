package types;

public class ObjTypeInfo extends TypeInfo {
    // Reference size of an object
    public static final int REF_SIZE = 4;

    public ObjTypeInfo(String id) {
        super(id, REF_SIZE);
    }
}
