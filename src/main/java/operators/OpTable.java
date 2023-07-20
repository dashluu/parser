package operators;

import toks.TokType;
import types.TypeInfo;
import types.TypeTable;

import java.util.HashMap;
import java.util.HashSet;

// Table for storing operators and their properties
public class OpTable {
    private static final HashMap<String, TokType> OP_MAP = new HashMap<>();
    private static final HashSet<TokType> PREFIX_OPS = new HashSet<>();
    private static final HashSet<TokType> INFIX_OPS = new HashSet<>();
    private static final HashSet<TokType> POSTFIX_OPS = new HashSet<>();
    // Precedence table
    private static final HashMap<TokType, Integer> PRECED_MAP = new HashMap<>();
    // Associativity table, true means left-to-right, false means right-to-left
    private static final HashMap<TokType, Boolean> ASSOCIATIVITY_MAP = new HashMap<>();
    // This table stores the data type compatibility for each operator
    // When an operator is applied, it is used to check if the operands' data types are compatible
    // If they are, it finds the data type of the result after applying the operator
    private static final HashMap<OpCompat, TypeInfo> COMPAT_MAP = new HashMap<>();
    private static final OpTable INST = new OpTable();
    private static boolean init = false;

    private OpTable() {
    }

    /**
     * Initializes the only instance of OpTable if it has not been initialized and then returns it.
     *
     * @return an OpTable object.
     */
    public static OpTable getInst() {
        if (!init) {
            // Add operators to table
            OP_MAP.put("!", TokType.LOG_NOT);
            OP_MAP.put("||", TokType.LOG_OR);
            OP_MAP.put("&&", TokType.LOG_AND);
            OP_MAP.put("==", TokType.EQ);
            OP_MAP.put("!=", TokType.NEQ);
            OP_MAP.put("<", TokType.LESS);
            OP_MAP.put(">", TokType.GREATER);
            OP_MAP.put("<=", TokType.LEQ);
            OP_MAP.put(">=", TokType.GEQ);
            OP_MAP.put("+", TokType.ADD);
            OP_MAP.put("-", TokType.SUB);
            OP_MAP.put("*", TokType.MUL);
            OP_MAP.put("/", TokType.DIV);
            OP_MAP.put("%", TokType.MOD);
            OP_MAP.put("<<", TokType.SHL);
            OP_MAP.put(">>", TokType.ART_SHR);
            OP_MAP.put(">>>", TokType.LOG_SHR);
            OP_MAP.put(".", TokType.DOT);
            OP_MAP.put(":", TokType.COLON);
            OP_MAP.put("=", TokType.ASSIGNMENT);
            OP_MAP.put("as", TokType.TYPE_CONV);
            OP_MAP.put("(", TokType.LPAREN);
            OP_MAP.put(")", TokType.RPAREN);
            OP_MAP.put("{", TokType.LBRACKETS);
            OP_MAP.put("}", TokType.RBRACKETS);
            OP_MAP.put(";", TokType.SEMICOLON);
            OP_MAP.put(",", TokType.COMMA);

            // Initialize prefix table
            PREFIX_OPS.add(TokType.ADD);
            PREFIX_OPS.add(TokType.SUB);
            PREFIX_OPS.add(TokType.LOG_NOT);

            // Initialize infix table
            INFIX_OPS.add(TokType.ASSIGNMENT);
            INFIX_OPS.add(TokType.LOG_OR);
            INFIX_OPS.add(TokType.LOG_AND);
            INFIX_OPS.add(TokType.EQ);
            INFIX_OPS.add(TokType.NEQ);
            INFIX_OPS.add(TokType.LESS);
            INFIX_OPS.add(TokType.GREATER);
            INFIX_OPS.add(TokType.LEQ);
            INFIX_OPS.add(TokType.GEQ);
            INFIX_OPS.add(TokType.ADD);
            INFIX_OPS.add(TokType.SUB);
            INFIX_OPS.add(TokType.MUL);
            INFIX_OPS.add(TokType.DIV);
            INFIX_OPS.add(TokType.MOD);
            INFIX_OPS.add(TokType.SHL);
            INFIX_OPS.add(TokType.ART_SHR);
            INFIX_OPS.add(TokType.LOG_SHR);
            INFIX_OPS.add(TokType.TYPE_CONV);

            // Initialize postfix table

            // Initialize precedence table
            PRECED_MAP.put(TokType.ASSIGNMENT, 10);
            PRECED_MAP.put(TokType.LOG_OR, 20);
            PRECED_MAP.put(TokType.LOG_AND, 30);
            PRECED_MAP.put(TokType.EQ, 40);
            PRECED_MAP.put(TokType.NEQ, 40);
            PRECED_MAP.put(TokType.LESS, 50);
            PRECED_MAP.put(TokType.GREATER, 50);
            PRECED_MAP.put(TokType.LEQ, 50);
            PRECED_MAP.put(TokType.GEQ, 50);
            PRECED_MAP.put(TokType.SHL, 60);
            PRECED_MAP.put(TokType.ART_SHR, 60);
            PRECED_MAP.put(TokType.LOG_SHR, 60);
            PRECED_MAP.put(TokType.ADD, 70);
            PRECED_MAP.put(TokType.SUB, 70);
            PRECED_MAP.put(TokType.MUL, 80);
            PRECED_MAP.put(TokType.DIV, 80);
            PRECED_MAP.put(TokType.MOD, 80);
            PRECED_MAP.put(TokType.TYPE_CONV, 90);

            // Initialize associativity table
            ASSOCIATIVITY_MAP.put(TokType.ASSIGNMENT, false);
            ASSOCIATIVITY_MAP.put(TokType.LOG_OR, true);
            ASSOCIATIVITY_MAP.put(TokType.LOG_AND, true);
            ASSOCIATIVITY_MAP.put(TokType.EQ, true);
            ASSOCIATIVITY_MAP.put(TokType.NEQ, true);
            ASSOCIATIVITY_MAP.put(TokType.LESS, true);
            ASSOCIATIVITY_MAP.put(TokType.GREATER, true);
            ASSOCIATIVITY_MAP.put(TokType.LEQ, true);
            ASSOCIATIVITY_MAP.put(TokType.GEQ, true);
            ASSOCIATIVITY_MAP.put(TokType.ADD, true);
            ASSOCIATIVITY_MAP.put(TokType.SUB, true);
            ASSOCIATIVITY_MAP.put(TokType.MUL, true);
            ASSOCIATIVITY_MAP.put(TokType.DIV, true);
            ASSOCIATIVITY_MAP.put(TokType.MOD, true);
            ASSOCIATIVITY_MAP.put(TokType.SHL, true);
            ASSOCIATIVITY_MAP.put(TokType.ART_SHR, true);
            ASSOCIATIVITY_MAP.put(TokType.LOG_SHR, true);
            ASSOCIATIVITY_MAP.put(TokType.TYPE_CONV, true);

            // Initialize operator type compatibility table
            TypeInfo intType = TypeTable.INT;
            TypeInfo floatType = TypeTable.FLOAT;
            TypeInfo boolType = TypeTable.BOOL;

            // Unary operators
            INST.registerCompat(new UnOpCompat(TokType.ADD, intType), intType);
            INST.registerCompat(new UnOpCompat(TokType.ADD, floatType), floatType);
            INST.registerCompat(new UnOpCompat(TokType.SUB, intType), intType);
            INST.registerCompat(new UnOpCompat(TokType.SUB, floatType), floatType);
            INST.registerCompat(new UnOpCompat(TokType.LOG_NOT, boolType), boolType);

            // Binary operators
            INST.registerCompat(new BinOpCompat(TokType.ADD, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.ADD, intType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.ADD, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.ADD, floatType, floatType), floatType);

            INST.registerCompat(new BinOpCompat(TokType.SUB, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.SUB, intType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.SUB, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.SUB, floatType, floatType), floatType);

            INST.registerCompat(new BinOpCompat(TokType.MUL, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.MUL, intType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.MUL, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.MUL, floatType, floatType), floatType);

            INST.registerCompat(new BinOpCompat(TokType.DIV, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.DIV, intType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.DIV, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.DIV, floatType, floatType), floatType);

            INST.registerCompat(new BinOpCompat(TokType.MOD, intType, intType), intType);

            INST.registerCompat(new BinOpCompat(TokType.EQ, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.EQ, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.EQ, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.EQ, floatType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.EQ, boolType, boolType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.NEQ, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.NEQ, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.NEQ, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.NEQ, floatType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.NEQ, boolType, boolType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.LESS, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LESS, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LESS, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LESS, floatType, floatType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.GREATER, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GREATER, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GREATER, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GREATER, floatType, floatType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.LEQ, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LEQ, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LEQ, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.LEQ, floatType, floatType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.GEQ, intType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GEQ, intType, floatType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GEQ, floatType, intType), boolType);
            INST.registerCompat(new BinOpCompat(TokType.GEQ, floatType, floatType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.LOG_OR, boolType, boolType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.LOG_AND, boolType, boolType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, intType, floatType), intType);
            INST.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, floatType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.ASSIGNMENT, boolType, boolType), boolType);

            INST.registerCompat(new BinOpCompat(TokType.TYPE_CONV, intType, intType), intType);
            INST.registerCompat(new BinOpCompat(TokType.TYPE_CONV, intType, floatType), intType);
            INST.registerCompat(new BinOpCompat(TokType.TYPE_CONV, floatType, intType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.TYPE_CONV, floatType, floatType), floatType);
            INST.registerCompat(new BinOpCompat(TokType.TYPE_CONV, boolType, boolType), boolType);

            init = true;
        }
        return INST;
    }

    /**
     * Checks if a given string is a prefix of any operator string in the table.
     *
     * @param prefixStr the prefix string.
     * @return true if the given string is a prefix of an operator string and false otherwise.
     */
    public boolean isOpPrefixStr(String prefixStr) {
        for (String opStr : OP_MAP.keySet()) {
            if (opStr.indexOf(prefixStr) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the operator's id associated with the given string.
     *
     * @param opStr a string associated with an operator.
     * @return a TokType object as the operator's id if it exists, otherwise, return null.
     */
    public TokType getId(String opStr) {
        return OP_MAP.get(opStr);
    }

    /**
     * Checks if a token is a prefix operator.
     *
     * @param id operator's id.
     * @return true if the token is a prefix operator and false otherwise.
     */
    public boolean isPrefixOp(TokType id) {
        return PREFIX_OPS.contains(id);
    }

    /**
     * Checks if a token is an infix operator.
     *
     * @param id operator's id.
     * @return true if the token is an infix operator and false otherwise.
     */
    public boolean isInfixOp(TokType id) {
        return INFIX_OPS.contains(id);
    }

    /**
     * Checks if a token is a postfix operator.
     *
     * @param id operator's id.
     * @return true if the token is a postfix operator and false otherwise.
     */
    public boolean isPostfixOp(TokType id) {
        return POSTFIX_OPS.contains(id);
    }

    /**
     * Gets the precedence of the given operator.
     *
     * @param id operator's id.
     * @return an int value representing the operator precedence.
     */
    public int getPreced(TokType id) {
        Integer preced = PRECED_MAP.get(id);
        return preced == null ? -1 : preced;
    }

    /**
     * Gets the associativity of the given operator.
     *
     * @param id operator's id.
     * @return true if the operator left-to-right, otherwise, return false.
     */
    public boolean getAssociativity(TokType id) {
        return ASSOCIATIVITY_MAP.get(id);
    }

    /**
     * Compares the precedences of two operators.
     *
     * @param id1 the first operator's id.
     * @param id2 the second operator's id.
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
        COMPAT_MAP.put(opCompat, resultDtype);
    }

    /**
     * Gets the result's data type after applying operator to operands with specific data types.
     *
     * @param opCompact the object that stores operator compatibility.
     * @return the result's data type.
     */
    public TypeInfo getCompatDtype(OpCompat opCompact) {
        return COMPAT_MAP.get(opCompact);
    }
}
