package symbols;

import types.TypeInfo;

public class ParamInfo extends SymbolInfo {
    private final long stackMem;

    public ParamInfo(String id, TypeInfo dtype, long stackMem) {
        super(id, SymbolType.PARAM, dtype);
        this.stackMem = stackMem;
    }

    public long getStackMem() {
        return stackMem;
    }
}
