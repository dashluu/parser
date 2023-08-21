package types;

import java.util.ArrayList;
import java.util.List;

public class ArrType extends ObjType {
    public static final String ID = "Array";
    // The core data type, or the data type of a non-array element in the array
    private TypeInfo coreDtype;
    // The data type of each element
    private TypeInfo elmDtype;
    private final List<Integer> dimList = new ArrayList<>();

    public ArrType(TypeInfo coreDtype, int dim) {
        super(ID);
        this.coreDtype = coreDtype;
        elmDtype = coreDtype;
        dimList.add(dim);
    }

    public ArrType(ArrType elmArrDtype) {
        super(ID);
        coreDtype = elmArrDtype.coreDtype;
        elmDtype = elmArrDtype;
        dimList.addAll(elmArrDtype.dimList);
    }

    public TypeInfo getCoreDtype() {
        return coreDtype;
    }

    public void setCoreDtype(TypeInfo coreDtype) {
        this.coreDtype = coreDtype;
    }

    public TypeInfo getElmDtype() {
        return elmDtype;
    }

    public void setElmDtype(TypeInfo elmDtype) {
        this.elmDtype = elmDtype;
    }

    public TypeInfo getNestedElmDtype(int startDim) {
        TypeInfo nestedElmDtype = this;
        for (int i = 0; i < startDim; ++i) {
            nestedElmDtype = ((ArrType) nestedElmDtype).getElmDtype();
        }
        return nestedElmDtype;
    }

    public void addDim(int dim) {
        dimList.add(dim);
    }

    public int getNumDims() {
        return dimList.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ArrType arrDtype)) {
            return false;
        }
        // Check if the dimension lists match
        if (!dimList.equals(arrDtype.dimList)) {
            return false;
        }
        // Check if the core data types match
        return coreDtype.equals(arrDtype.coreDtype);
    }
}
