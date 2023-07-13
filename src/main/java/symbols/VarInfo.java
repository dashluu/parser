package symbols;

import types.TypeInfo;

public class VarInfo extends SymbolInfo {
    private final int label;

    public VarInfo(String id, TypeInfo dtype, int label) {
        super(id, SymbolType.VAR, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }
}
