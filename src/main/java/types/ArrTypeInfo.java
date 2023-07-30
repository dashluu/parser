package types;

import java.util.ArrayList;
import java.util.List;

public class ArrTypeInfo extends ObjTypeInfo {
    public static final String ID = "Array";
    private final List<Integer> dimList = new ArrayList<>();

    public ArrTypeInfo() {
        super(ID);
    }

    /**
     * Creates a new array data type whose dimension is reduced from a given array data type.
     *
     * @param dimIndex the starting dimension index of the given array data type.
     * @return an ArrTypeInfo object.
     */
    public static ArrTypeInfo createReducedDimArrType(ArrTypeInfo arrTypeInfo, int dimIndex) {
        ArrTypeInfo newArrTypeInfo = new ArrTypeInfo();
        for (int i = dimIndex; i < arrTypeInfo.dimList.size(); ++i) {
            newArrTypeInfo.dimList.add(arrTypeInfo.dimList.get(i));
        }
        return newArrTypeInfo;
    }
}
