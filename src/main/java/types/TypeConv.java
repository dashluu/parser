package types;

/**
 * @param srcDtype  The source type
 * @param destDtype The destination type
 * @param explicit  Whether the type conversion is explicit
 */ // Type conversion
public record TypeConv(TypeInfo srcDtype, TypeInfo destDtype, boolean explicit) {

    @Override
    public int hashCode() {
        String hashStr = srcDtype.id() + destDtype.id();
        return hashStr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeConv typeConv)) {
            return false;
        }
        return srcDtype.equals(typeConv.srcDtype) && destDtype.equals(typeConv.destDtype);
    }
}
