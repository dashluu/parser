package operators;

import toks.TokType;
import types.TypeInfo;
import types.TypeTable;

import java.util.HashMap;
import java.util.HashSet;

// Table for storing operators and their properties
public class OpTable {
    private final HashMap<String, TokType> opMap = new HashMap<>();
    private final HashSet<TokType> prefixOps = new HashSet<>();
    private final HashSet<TokType> infixOps = new HashSet<>();
    private final HashSet<TokType> postfixOps = new HashSet<>();
    // Precedence table
    private final HashMap<TokType, Integer> precedMap = new HashMap<>();
    // Associativity table, true means left-to-right, false means right-to-left
    private final HashMap<TokType, Boolean> associativityMap = new HashMap<>();
    // This table stores the data type compatibility for each operator
    // When an operator is applied, it is used to check if the operands' data types are compatible
    // If they are, it finds the data type of the result after applying the operator
    private final HashMap<OpCompat, TypeInfo> compatMap = new HashMap<>();

    private OpTable() {
    }

    /**
     * Creates an instance of OpTable and initializes it.
     *
     * @return an OpTable object.
     */
    public static OpTable createTable() {
        OpTable table = new OpTable();
        // Add operators to table
        table.opMap.put("!", TokType.LOG_NOT);
        table.opMap.put("||", TokType.LOG_OR);
        table.opMap.put("&&", TokType.LOG_AND);
        table.opMap.put("==", TokType.EQ);
        table.opMap.put("!=", TokType.NEQ);
        table.opMap.put("<", TokType.LESS);
        table.opMap.put(">", TokType.GREATER);
        table.opMap.put("<=", TokType.LEQ);
        table.opMap.put(">=", TokType.GEQ);
        table.opMap.put("+", TokType.ADD);
        table.opMap.put("-", TokType.SUB);
        table.opMap.put("*", TokType.MUL);
        table.opMap.put("/", TokType.DIV);
        table.opMap.put("%", TokType.MOD);
        table.opMap.put("<<", TokType.SHL);
        table.opMap.put(">>", TokType.ART_SHR);
        table.opMap.put(">>>", TokType.LOG_SHR);
        table.opMap.put(".", TokType.DOT);
        table.opMap.put(":", TokType.COLON);
        table.opMap.put("=", TokType.ASSIGNMENT);
        table.opMap.put("as", TokType.TYPE_CONV);
        table.opMap.put("(", TokType.LPAREN);
        table.opMap.put(")", TokType.RPAREN);
        table.opMap.put("{", TokType.LBRACKETS);
        table.opMap.put("}", TokType.RBRACKETS);
        table.opMap.put(";", TokType.SEMICOLON);
        table.opMap.put(",", TokType.COMMA);

        // Initialize prefix table
        table.prefixOps.add(TokType.ADD);
        table.prefixOps.add(TokType.SUB);
        table.prefixOps.add(TokType.LOG_NOT);

        // Initialize infix table
        table.infixOps.add(TokType.ASSIGNMENT);
        table.infixOps.add(TokType.LOG_OR);
        table.infixOps.add(TokType.LOG_AND);
        table.infixOps.add(TokType.EQ);
        table.infixOps.add(TokType.NEQ);
        table.infixOps.add(TokType.LESS);
        table.infixOps.add(TokType.GREATER);
        table.infixOps.add(TokType.LEQ);
        table.infixOps.add(TokType.GEQ);
        table.infixOps.add(TokType.ADD);
        table.infixOps.add(TokType.SUB);
        table.infixOps.add(TokType.MUL);
        table.infixOps.add(TokType.DIV);
        table.infixOps.add(TokType.MOD);
        table.infixOps.add(TokType.SHL);
        table.infixOps.add(TokType.ART_SHR);
        table.infixOps.add(TokType.LOG_SHR);
        table.infixOps.add(TokType.TYPE_CONV);

        // Initialize postfix table

        // Initialize precedence table
        table.precedMap.put(TokType.ASSIGNMENT, 10);
        table.precedMap.put(TokType.LOG_OR, 20);
        table.precedMap.put(TokType.LOG_AND, 30);
        table.precedMap.put(TokType.EQ, 40);
        table.precedMap.put(TokType.NEQ, 40);
        table.precedMap.put(TokType.LESS, 50);
        table.precedMap.put(TokType.GREATER, 50);
        table.precedMap.put(TokType.LEQ, 50);
        table.precedMap.put(TokType.GEQ, 50);
        table.precedMap.put(TokType.SHL, 60);
        table.precedMap.put(TokType.ART_SHR, 60);
        table.precedMap.put(TokType.LOG_SHR, 60);
        table.precedMap.put(TokType.ADD, 70);
        table.precedMap.put(TokType.SUB, 70);
        table.precedMap.put(TokType.MUL, 80);
        table.precedMap.put(TokType.DIV, 80);
        table.precedMap.put(TokType.MOD, 80);
        table.precedMap.put(TokType.TYPE_CONV, 90);

        // Initialize associativity table
        table.associativityMap.put(TokType.ASSIGNMENT, false);
        table.associativityMap.put(TokType.LOG_OR, true);
        table.associativityMap.put(TokType.LOG_AND, true);
        table.associativityMap.put(TokType.EQ, true);
        table.associativityMap.put(TokType.NEQ, true);
        table.associativityMap.put(TokType.LESS, true);
        table.associativityMap.put(TokType.GREATER, true);
        table.associativityMap.put(TokType.LEQ, true);
        table.associativityMap.put(TokType.GEQ, true);
        table.associativityMap.put(TokType.ADD, true);
        table.associativityMap.put(TokType.SUB, true);
        table.associativityMap.put(TokType.MUL, true);
        table.associativityMap.put(TokType.DIV, true);
        table.associativityMap.put(TokType.MOD, true);
        table.associativityMap.put(TokType.SHL, true);
        table.associativityMap.put(TokType.ART_SHR, true);
        table.associativityMap.put(TokType.LOG_SHR, true);
        table.associativityMap.put(TokType.TYPE_CONV, true);

        // Initialize operator type compatibility table
        TypeInfo intType = TypeTable.INT;
        TypeInfo floatType = TypeTable.FLOAT;
        TypeInfo boolType = TypeTable.BOOL;

        // Unary operators
        table.registerCompat(new UnOpCompat(TokType.ADD, intType), intType);
        table.registerCompat(new UnOpCompat(TokType.ADD, floatType), floatType);
        table.registerCompat(new UnOpCompat(TokType.SUB, intType), intType);
        table.registerCompat(new UnOpCompat(TokType.SUB, floatType), floatType);
        table.registerCompat(new UnOpCompat(TokType.LOG_NOT, boolType), boolType);

        // Binary operators
        table.registerCompat(new BinOpCompat(TokType.ADD, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.ADD, intType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.ADD, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.ADD, floatType, floatType), floatType);

        table.registerCompat(new BinOpCompat(TokType.SUB, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.SUB, intType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.SUB, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.SUB, floatType, floatType), floatType);

        table.registerCompat(new BinOpCompat(TokType.MUL, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.MUL, intType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.MUL, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.MUL, floatType, floatType), floatType);

        table.registerCompat(new BinOpCompat(TokType.DIV, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.DIV, intType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.DIV, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.DIV, floatType, floatType), floatType);

        table.registerCompat(new BinOpCompat(TokType.MOD, intType, intType), intType);

        table.registerCompat(new BinOpCompat(TokType.EQ, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.EQ, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.EQ, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.EQ, floatType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.EQ, boolType, boolType), boolType);

        table.registerCompat(new BinOpCompat(TokType.NEQ, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.NEQ, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.NEQ, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.NEQ, floatType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.NEQ, boolType, boolType), boolType);

        table.registerCompat(new BinOpCompat(TokType.LESS, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LESS, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LESS, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LESS, floatType, floatType), boolType);

        table.registerCompat(new BinOpCompat(TokType.GREATER, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GREATER, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GREATER, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GREATER, floatType, floatType), boolType);

        table.registerCompat(new BinOpCompat(TokType.LEQ, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LEQ, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LEQ, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.LEQ, floatType, floatType), boolType);

        table.registerCompat(new BinOpCompat(TokType.GEQ, intType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GEQ, intType, floatType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GEQ, floatType, intType), boolType);
        table.registerCompat(new BinOpCompat(TokType.GEQ, floatType, floatType), boolType);

        table.registerCompat(new BinOpCompat(TokType.LOG_OR, boolType, boolType), boolType);

        table.registerCompat(new BinOpCompat(TokType.LOG_AND, boolType, boolType), boolType);

        table.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, intType, floatType), intType);
        table.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, floatType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, boolType, boolType), boolType);

        table.registerCompat(new BinOpCompat(TokType.TYPE_CONV, intType, intType), intType);
        table.registerCompat(new BinOpCompat(TokType.TYPE_CONV, intType, floatType), intType);
        table.registerCompat(new BinOpCompat(TokType.TYPE_CONV, floatType, intType), floatType);
        table.registerCompat(new BinOpCompat(TokType.TYPE_CONV, floatType, floatType), floatType);
        table.registerCompat(new BinOpCompat(TokType.TYPE_CONV, boolType, boolType), boolType);

        return table;
    }

    /**
     * Checks if a given string is a prefix of any operator string in the table.
     *
     * @param prefixStr the prefix string.
     * @return true if the given string is a prefix of an operator string and false otherwise.
     */
    public boolean isOpPrefixStr(String prefixStr) {
        for (String opStr : opMap.keySet()) {
            if (opStr.indexOf(prefixStr) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the operator's identifier associated with the given string.
     *
     * @param opStr a string associated with an operator.
     * @return a TokType object as the operator's identifier if it exists, otherwise, return null.
     */
    public TokType getId(String opStr) {
        return opMap.get(opStr);
    }

    /**
     * Checks if a token is a prefix operator.
     *
     * @param id operator's identifier.
     * @return true if the token is a prefix operator and false otherwise.
     */
    public boolean isPrefixOp(TokType id) {
        return prefixOps.contains(id);
    }

    /**
     * Checks if a token is an infix operator.
     *
     * @param id operator's identifier.
     * @return true if the token is an infix operator and false otherwise.
     */
    public boolean isInfixOp(TokType id) {
        return infixOps.contains(id);
    }

    /**
     * Checks if a token is a postfix operator.
     *
     * @param id operator's identifier.
     * @return true if the token is a postfix operator and false otherwise.
     */
    public boolean isPostfixOp(TokType id) {
        return postfixOps.contains(id);
    }

    /**
     * Gets the precedence of the given operator.
     *
     * @param id operator's identifier.
     * @return an int value representing the operator precedence.
     */
    public int getPreced(TokType id) {
        Integer preced = precedMap.get(id);
        return preced == null ? -1 : preced;
    }

    /**
     * Gets the associativity of the given operator.
     *
     * @param id operator's identifier.
     * @return true if the operator left-to-right, otherwise, return false.
     */
    public boolean getAssociativity(TokType id) {
        return associativityMap.get(id);
    }

    /**
     * Compares the precedences of two operators.
     *
     * @param id1 the first operator's identifier.
     * @param id2 the second operator's identifier.
     * @return 1 if the first operator has higher priority, otherwise, return -1.
     */
    public int cmpPreced(TokType id1, TokType id2) {
        int preced1 = getPreced(id1);
        int preced2 = getPreced(id2);
        if (preced1 != preced2) {
            // If the two precedences are not the same,
            // return 1 if the first operator has higher precedence, otherwise, return -1
            return Integer.compare(preced1, preced2);
        }
        // Get the associativity of the first operator
        boolean leftToRight = getAssociativity(id1);
        // Return 1 if the first operator is left-to-right, otherwise, return -1
        return leftToRight ? -1 : 1;
    }

    /**
     * Maps an operator compatibility(OpCompat) object to a data type.
     *
     * @param opCompat    the object that stores operator compatibility.
     * @param resultDtype the result's data type after applying the operator.
     */
    private void registerCompat(OpCompat opCompat, TypeInfo resultDtype) {
        compatMap.put(opCompat, resultDtype);
    }

    /**
     * Gets the result's data type after applying operator to operands with specific data types.
     *
     * @param opCompact the object that stores operator compatibility.
     * @return the result's data type.
     */
    public TypeInfo getCompatDtype(OpCompat opCompact) {
        return compatMap.get(opCompact);
    }
}
