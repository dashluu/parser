package symbols;

import types.TypeInfo;

public class ConstInfo extends SymbolInfo {
    public ConstInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.CONST, dtype);
    }
}
