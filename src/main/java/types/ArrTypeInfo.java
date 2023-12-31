package types;

public class ArrTypeInfo extends TypeInfo {
    public static final String ID = "Array";
    // Size of an array reference
    public static final int SIZE = 4;
    private TypeInfo coreDtype;
    private int dim;

    public ArrTypeInfo(TypeInfo coreDtype, int dim) {
        super(ID, TypeInfoType.ARR, SIZE);
        this.coreDtype = coreDtype;
        this.dim = dim;
    }

    public TypeInfo getCoreDtype() {
        return coreDtype;
    }

    public void setCoreDtype(TypeInfo coreDtype) {
        this.coreDtype = coreDtype;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ArrTypeInfo arrDtype)) {
            return false;
        }
        return coreDtype.equals(arrDtype.coreDtype) && dim == arrDtype.dim;
    }
}
