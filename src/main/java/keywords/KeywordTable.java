package keywords;

import toks.TokType;

import java.util.HashMap;

// Table for storing keywords
public class KeywordTable {
    private final HashMap<String, TokType> kwMap = new HashMap<>();
    // List of keywords for direct access
    public static final String VAR = "var";
    public static final String CONST = "let";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String FUN = "fun";
    public static final String RET = "return";
    public static final String IF = "if";
    public static final String ELIF = "elif";
    public static final String ELSE = "else";
    public static final String WHILE = "while";
    public static final String BREAK = "break";
    public static final String CONT = "continue";

    private KeywordTable() {
    }

    /**
     * Creates an instance of KeywordTable and initializes it.
     *
     * @return a KeywordTable object.
     */
    public static KeywordTable createTable() {
        KeywordTable table = new KeywordTable();
        // Add keywords to table
        table.kwMap.put(VAR, TokType.VAR_DECL);
        table.kwMap.put(CONST, TokType.CONST_DECL);
        table.kwMap.put(TRUE, TokType.BOOL_LITERAL);
        table.kwMap.put(FALSE, TokType.BOOL_LITERAL);
        table.kwMap.put(FUN, TokType.FUN_DECL);
        table.kwMap.put(RET, TokType.RET);
        table.kwMap.put(IF, TokType.IF);
        table.kwMap.put(ELIF, TokType.ELIF);
        table.kwMap.put(ELSE, TokType.ELSE);
        table.kwMap.put(WHILE, TokType.WHILE);
        table.kwMap.put(BREAK, TokType.BREAK);
        table.kwMap.put(CONT, TokType.CONT);
        return table;
    }

    /**
     * Gets the keyword's identifier associated with the given string.
     *
     * @param kwStr a string associated with a keyword.
     * @return a TokType object as the keyword's identifier if it exists, otherwise, return null.
     */
    public TokType getId(String kwStr) {
        return kwMap.get(kwStr);
    }
}
