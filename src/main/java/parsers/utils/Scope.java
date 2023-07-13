package parsers.utils;

import symbols.SymbolTable;
import types.TypeInfo;

// Code scope
public class Scope {
    private final SymbolTable symbolTable;
    // Hold the return type of the function surrounding the scope
    private TypeInfo retType;

    public Scope(Scope parent, TypeInfo retType) {
        this.retType = retType;
        this.symbolTable = new SymbolTable(parent == null ? null : parent.symbolTable);
    }

    public Scope(Scope parent) {
        this(parent, null);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setRetType(TypeInfo retType) {
        this.retType = retType;
    }

    public TypeInfo getRetType() {
        return retType;
    }
}
