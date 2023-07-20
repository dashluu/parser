package operators;

import toks.TokType;
import types.TypeInfo;

// Binary operator compatibility between two data types
public class BinOpCompat extends OpCompat {
    private final TypeInfo leftDtype;
    private final TypeInfo rightDtype;

    public BinOpCompat(TokType id, TypeInfo leftDtype, TypeInfo rightDtype) {
        super(id, OpCompatType.BINARY);
        this.leftDtype = leftDtype;
        this.rightDtype = rightDtype;
    }

    public TypeInfo getLeftDtype() {
        return leftDtype;
    }

    public TypeInfo getRightDtype() {
        return rightDtype;
    }

    @Override
    public int hashCode() {
        String hashStr = id + leftDtype.id() + rightDtype.id();
        return hashStr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BinOpCompat binOpTypeCompat)) {
            return false;
        }
        return leftDtype.equals(binOpTypeCompat.leftDtype) &&
                rightDtype.equals(binOpTypeCompat.rightDtype);
    }
}
