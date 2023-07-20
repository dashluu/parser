package utils;

import operators.OpTable;
import types.TypeTable;

public class Context {
    private final ScopeStack scopeStack = new ScopeStack();
    private final TypeTable typeTable = TypeTable.getInst();
    private final OpTable opTable = OpTable.getInst();

    public ScopeStack getScopeStack() {
        return scopeStack;
    }

    /**
     * Gets the current context's scope.
     *
     * @return a Scope object.
     */
    public Scope getScope() {
        return scopeStack.peek();
    }

    public TypeTable getTypeTable() {
        return typeTable;
    }

    public OpTable getOpTable() {
        return opTable;
    }
}
