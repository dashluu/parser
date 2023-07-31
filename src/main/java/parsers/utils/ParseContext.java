package parsers.utils;

import exceptions.ErrMsg;
import keywords.KeywordTable;
import operators.OpTable;
import types.TypeTable;

public class ParseContext {
    private ScopeStack scopeStack;
    private TypeTable typeTable;
    private OpTable opTable;
    private KeywordTable kwTable;
    private ErrMsg errMsg = null;

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

    /**
     * Updates the error message to the earliest one as possible and also returns an error signal.
     *
     * @param msg the error message.
     * @param <E> type argument to the ParseResult object.
     * @return a ParseResult object as an error signal.
     */
    public <E> ParseResult<E> raiseErr(ErrMsg msg) {
        if (errMsg == null ||
                errMsg.getSrcPos().getLn() > msg.getSrcPos().getLn() ||
                errMsg.getSrcPos().getCol() > msg.getSrcPos().getCol()) {
            // Updates the error to the earliest one as possible, that is, one with <= line and <= column
            errMsg = msg;
        }
        return ParseResult.err();
    }

    /**
     * Checks if there is an error during parsing.
     *
     * @return true if there is an error and false otherwise.
     */
    public boolean hasErr() {
        return errMsg != null;
    }

    public ErrMsg getErrMsg() {
        return errMsg;
    }
}
