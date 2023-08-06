package parsers.scope;

import symbols.SymbolTable;
import types.TypeInfo;

// Code scope
public class Scope {
    protected final ScopeType scopeType;
    protected final Scope parent;
    protected final SymbolTable symbolTable;
    protected RetState retState = RetState.INITIAL;

    public Scope(ScopeType scopeType, Scope parent) {
        this.scopeType = scopeType;
        this.parent = parent;
        this.symbolTable = new SymbolTable(parent == null ? null : parent.symbolTable);
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public Scope getParent() {
        return parent;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public RetState getRetState() {
        return retState;
    }

    public void setRetState(RetState retState) {
        this.retState = retState;
    }

    /**
     * Determines if the current scope is inside a function.
     *
     * @return the return type of the function if it is in one and null otherwise.
     */
    public TypeInfo isInFun() {
        Scope upScope = this;
        while (upScope != null) {
            if (upScope.scopeType == ScopeType.FUNCTION) {
                FunScope funScope = (FunScope) upScope;
                return funScope.getRetDtype();
            }
            upScope = upScope.parent;
        }
        return null;
    }

    /**
     * Determines if the current scope is inside a loop.
     *
     * @return true if it is inside a loop and false otherwise.
     */
    public boolean isInLoop() {
        Scope upScope = this;
        while (upScope != null) {
            if (upScope.scopeType == ScopeType.LOOP) {
                return true;
            }
            upScope = upScope.parent;
        }
        return false;
    }
}
