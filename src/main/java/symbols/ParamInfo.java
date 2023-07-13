package symbols;

import types.TypeInfo;

public class ParamInfo extends SymbolInfo {
    private final int label;

    public ParamInfo(String id, TypeInfo dtype, int label) {
        super(id, SymbolType.PARAM, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }
}
