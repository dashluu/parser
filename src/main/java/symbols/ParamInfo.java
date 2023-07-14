package symbols;

import types.TypeInfo;

// A class for storing parameter information in the symbol table
public class ParamInfo extends SymbolInfo {
    public ParamInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.PARAM, dtype);
    }
}
