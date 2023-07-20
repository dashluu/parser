package utils;

import keywords.KeywordTable;
import operators.OpTable;
import types.TypeTable;

public class ParseContext {
    private ScopeStack scopeStack;
    private TypeTable typeTable;
    private OpTable opTable;
    private KeywordTable kwTable;

    private ParseContext() {
    }

    /**
     * Creates an instance of ParseContext and initializes it.
     *
     * @return a ParseContext object.
     */
    public static ParseContext createContext() {
        ParseContext context = new ParseContext();
        context.scopeStack = new ScopeStack();
        context.typeTable = TypeTable.createTable();
        context.opTable = OpTable.createTable();
        context.kwTable = KeywordTable.createTable();
        return context;
    }

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

    public KeywordTable getKeywordTable() {
        return kwTable;
    }
}
