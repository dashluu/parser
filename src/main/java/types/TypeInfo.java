package types;

// Data type
public record TypeInfo(String id, int size) {

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
