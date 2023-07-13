package symbols;

import types.TypeInfo;

public class ConstInfo extends SymbolInfo {
    private final int label;

    public ConstInfo(String id, TypeInfo dtype, int label) {
        super(id, SymbolType.CONST, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }
}
