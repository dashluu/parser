package parsers.utils;

import symbols.SymbolTable;
import types.TypeInfo;

// Code scope
public class Scope {
    private final SymbolTable symbolTable;
    // Hold the return type of the function surrounding the scope
    private TypeInfo retDtype;
    private boolean inLoop;

    public Scope(Scope parent) {
        this.inLoop = (parent != null && parent.isInLoop());
        this.retDtype = (parent == null ? null : parent.retDtype);
        this.symbolTable = new SymbolTable(parent == null ? null : parent.symbolTable);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setRetDtype(TypeInfo retDtype) {
        this.retDtype = retDtype;
    }

    public TypeInfo getRetDtype() {
        return retDtype;
    }

    public boolean isInLoop() {
        return inLoop;
    }

    public void setInLoop(boolean inLoop) {
        this.inLoop = inLoop;
    }
}
