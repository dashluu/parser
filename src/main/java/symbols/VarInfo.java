package symbols;

import types.TypeInfo;

public class VarInfo extends SymbolInfo {
    private final long stackMem;

    public VarInfo(String id, TypeInfo dtype, long stackMem) {
        super(id, SymbolType.VAR, dtype);
        this.stackMem = stackMem;
    }

    public long getStackMem() {
        return stackMem;
    }
}
