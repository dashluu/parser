package symbols;

import types.TypeInfo;

public class VarInfo extends SymbolInfo {
    public VarInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.VAR, dtype);
    }
}
