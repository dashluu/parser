package symbols;

import types.TypeInfo;

// A class for storing constant information in the symbol table
public class ConstInfo extends SymbolInfo {
    public ConstInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.CONST, dtype);
    }
}
