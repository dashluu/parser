package symbols;

import types.TypeInfo;

public class ConstInfo extends SymbolInfo {
    private final long stackMem;

    public ConstInfo(String id, TypeInfo dtype, long stackMem) {
        super(id, SymbolType.CONST, dtype);
        this.stackMem = stackMem;
    }

    public long getStackMem() {
        return stackMem;
    }
}
