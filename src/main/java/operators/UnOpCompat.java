package operators;

import toks.TokType;
import types.TypeInfo;

// Unary operator compatibility
public class UnOpCompat extends OpCompat {
    private final TypeInfo dtype;

    public UnOpCompat(TokType id, TypeInfo dtype) {
        super(id, OpCompatType.UNARY);
        this.dtype = dtype;
    }

    public TypeInfo getDtype() {
        return dtype;
    }

    @Override
    public int hashCode() {
        String hashStr = id + dtype.getId();
        return hashStr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof UnOpCompat unOpTypeCompat)) {
            return false;
        }
        return dtype.equals(unOpTypeCompat.dtype);
    }
}
