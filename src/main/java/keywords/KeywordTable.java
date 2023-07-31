package keywords;

import toks.TokType;

import java.util.HashMap;

// Table for storing keywords
public class KeywordTable {
    private final HashMap<String, TokType> kwMap = new HashMap<>();
    // List of keywords for direct access
    public static final String VAR = "var", CONST = "let",
            TRUE = "true", FALSE = "false",
            FUN = "fun", RET = "return",
            IF = "if", ELIF = "elif", ELSE = "else", WHILE = "while",
            BREAK = "break", CONT = "continue";

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
