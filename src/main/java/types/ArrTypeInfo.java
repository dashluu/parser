package types;

import java.util.ArrayList;
import java.util.List;

public class ArrTypeInfo extends ObjTypeInfo {
    public static final String ID = "Array";
    private final List<Integer> dimList = new ArrayList<>();

    public ArrTypeInfo() {
        super(ID);
    }
}
