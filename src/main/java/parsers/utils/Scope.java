package parsers.utils;

import symbols.SymbolTable;
import types.TypeInfo;

// Code scope
public class Scope {
    private final SymbolTable symbolTable;
    private final MemTable memTable;
    private final Scope parent;
    // Hold the return type of the function surrounding the scope
    private TypeInfo retType;

    public Scope(Scope parent, TypeInfo retType) {
        this.parent = parent;
        this.retType = retType;
        this.symbolTable = new SymbolTable(parent == null ? null : parent.symbolTable);
        this.memTable = new MemTable(parent == null ? null : parent.memTable);
    }

    public Scope(Scope parent) {
        this(parent, null);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public MemTable getMemTable() {
        return memTable;
    }

    public Scope getParent() {
        return parent;
    }

    public void setRetType(TypeInfo retType) {
        this.retType = retType;
    }

    public TypeInfo getRetType() {
        return retType;
    }
}
