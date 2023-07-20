package keywords;

import toks.TokType;

import java.util.HashMap;

// Table for storing keywords
public class KeywordTable {
    private static final HashMap<String, TokType> KW_MAP = new HashMap<>();
    private static final KeywordTable INST = new KeywordTable();
    private static boolean init = false;
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
     * Initializes the only instance of KeywordTable if it has not been initialized and then returns it.
     *
     * @return a KeywordTable object.
     */
    public static KeywordTable getInst() {
        if (!init) {
            // Add keywords to table
            KW_MAP.put(VAR, TokType.VAR_DECL);
            KW_MAP.put(CONST, TokType.CONST_DECL);
            KW_MAP.put(TRUE, TokType.BOOL_LITERAL);
            KW_MAP.put(FALSE, TokType.BOOL_LITERAL);
            KW_MAP.put(FUN, TokType.FUN_DECL);
            KW_MAP.put(RET, TokType.RET);
            KW_MAP.put(IF, TokType.IF);
            KW_MAP.put(ELIF, TokType.ELIF);
            KW_MAP.put(ELSE, TokType.ELSE);
            KW_MAP.put(WHILE, TokType.WHILE);
            KW_MAP.put(BREAK, TokType.BREAK);
            KW_MAP.put(CONT, TokType.CONT);

            init = true;
        }
        return INST;
    }

    /**
     * Gets the keyword's id associated with the given string.
     *
     * @param kwStr a string associated with a keyword.
     * @return a TokType object as the keyword's id if it exists, otherwise, return null.
     */
    public TokType getId(String kwStr) {
        return KW_MAP.get(kwStr);
    }
}
