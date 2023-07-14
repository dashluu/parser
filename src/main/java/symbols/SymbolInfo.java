package symbols;

import types.TypeInfo;

// The base class for storing symbol information in the symbol table
public class SymbolInfo {
    protected final String id;
    protected final SymbolType symbolType;
    // The data type of the symbol
    // For a function, this is its return type
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
