package symbols;

import types.TypeInfo;

public class SymbolInfo {
    protected final String id;
    protected final SymbolType symbolType;
    protected TypeInfo dtype;

    public SymbolInfo(String id, SymbolType symbolType, TypeInfo dtype) {
        this.id = id;
        this.symbolType = symbolType;
        this.dtype = dtype;
    }

    public String getId() {
        return id;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public TypeInfo getDtype() {
        return dtype;
    }

    public void setDtype(TypeInfo dtype) {
        this.dtype = dtype;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SymbolInfo symbol)) {
            return false;
        }
        return id.equals(symbol.id);
    }
}
