package symbols;

import types.TypeInfo;

// The base class for storing symbol information in the symbol table
public class SymbolInfo {
    protected final String id;
    protected final SymbolType symbolType;
    // The data type of the symbol
    // For a function, this is its return type
    protected TypeInfo dtype;
    protected boolean mutable;

    public SymbolInfo(String id, SymbolType symbolType, TypeInfo dtype, boolean mutable) {
        this.id = id;
        this.symbolType = symbolType;
        this.dtype = dtype;
        this.mutable = mutable;
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

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
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
