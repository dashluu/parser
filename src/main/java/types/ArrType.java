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

    public ArrType(TypeInfo dtype) {
        super(ID);
        if (dtype == null || !dtype.getId().equals(ID)) {
            coreDtype = dtype;
            elmDtype = dtype;
        } else {
            ArrType elmArrDtype = (ArrType) dtype;
            coreDtype = elmArrDtype.coreDtype;
            elmDtype = elmArrDtype;
            dimList.addAll(elmArrDtype.dimList);
        }
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

    /**
     * Gets an array element's data type starting from the given dimension.
     *
     * @param startDim the starting dimension used to determine the array element's data type.
     * @return a data type.
     */
    public TypeInfo getNestedElmDtype(int startDim) {
        TypeInfo nestedElmDtype = this;
        for (int i = 0; i < startDim; ++i) {
            nestedElmDtype = ((ArrType) nestedElmDtype).getElmDtype();
        }
        return nestedElmDtype;
    }

    public void addFirstDim(int dim) {
        dimList.add(0, dim);
    }

    public int getNumDims() {
        return dimList.size();
    }

    /**
     * Checks if this array data type is homogeneous with another array data type.
     *
     * @param arrDtype the other array data type to be checked with.
     * @return true if the two arrays are homogeneous and false otherwise.
     */
    public boolean homogeneousWith(ArrType arrDtype) {
        if (!equals(arrDtype)) {
            return false;
        }
        // Check if the dimension lists match
        return dimList.equals(arrDtype.dimList);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ArrType arrDtype)) {
            return false;
        }
        // Check if the numbers of dimensions match
        if (getNumDims() != arrDtype.getNumDims()) {
            return false;
        }
        // Check if the core data types match
        return coreDtype.equals(arrDtype.coreDtype);
    }
}
