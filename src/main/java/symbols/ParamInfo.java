package symbols;

import types.TypeInfo;

public class ParamInfo extends SymbolInfo {
    public ParamInfo(String id, TypeInfo dtype) {
        super(id, SymbolType.PARAM, dtype);
    }
}
