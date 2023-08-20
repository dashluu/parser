package types;

public class ArrTypeInfo extends ObjTypeInfo {
    public static final String ID = "Array";
    private TypeInfo coreDtype;

    public ArrTypeInfo(TypeInfo coreDtype) {
        super(ID);
        this.coreDtype = coreDtype;
    }

    public TypeInfo getCoreDtype() {
        return coreDtype;
    }

    public void setCoreDtype(TypeInfo coreDtype) {
        this.coreDtype = coreDtype;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ArrTypeInfo arrDtype)) {
            return false;
        }
        return coreDtype.equals(arrDtype.coreDtype);
    }
}
