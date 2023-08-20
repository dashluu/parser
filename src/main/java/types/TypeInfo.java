package types;

// Data type
public class TypeInfo {
    private final String id;
    private final boolean primitive;
    private final int size;

    public TypeInfo(String id, boolean primitive, int size) {
        this.id = id;
        this.primitive = primitive;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeInfo typeInfo)) {
            return false;
        }
        return id.equals(typeInfo.id);
    }
}
