package symbols;

import types.TypeInfo;

// A class for storing variable information in the symbol table
public class VarInfo extends SymbolInfo {
    public VarInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.VAR, dtype);
    }
}
