package types;

// Data type
public class TypeInfo {
    private final String id;
    private final TypeInfoType infoType;
    private final int size;

    public TypeInfo(String id, TypeInfoType infoType, int size) {
        this.id = id;
        this.infoType = infoType;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public TypeInfoType getInfoType() {
        return infoType;
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
