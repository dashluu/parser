package types;

import java.util.ArrayList;
import java.util.List;

public class ArrTypeInfo extends ObjTypeInfo {
    public static final String ID = "Array";
    private final int dim;

    public ArrTypeInfo(int dim) {
        super(ID);
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }
}
