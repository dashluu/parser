package types;

public class ArrType extends ObjType {
    public static final String ID = "Array";
    private TypeInfo elmDtype;

    public ArrType(TypeInfo elmDtype) {
        super(ID);
        this.elmDtype = elmDtype;
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

    /**
     * Gets the number of dimensions.
     *
     * @return a positive integer as the number of dimensions.
     */
    public int getNumDims() {
        int numDims = 0;
        TypeInfo dtype = this;
        ArrType arrDtype;

        while (dtype.getId().equals(ID)) {
            arrDtype = (ArrType) dtype;
            dtype = arrDtype.elmDtype;
            ++numDims;
        }

        return numDims;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ArrType arrDtype)) {
            return false;
        }
        // We don't check for the number of elements per dimension
        return elmDtype.equals(arrDtype.elmDtype);
    }
}
