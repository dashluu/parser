package types;

import toks.TokType;

import java.util.HashMap;

public class TypeTable {
    // String-to-type map
    private final HashMap<String, TypeInfo> strToType = new HashMap<>();
    private final HashMap<TokType, TypeInfo> literalToType = new HashMap<>();

    private TypeTable() {
    }

    /**
     * Creates an instance of TypeTable and initializes it.
     *
     * @return a TypeTable object.
     */
    public static TypeTable createTable() {
        TypeTable table = new TypeTable();
        // Add types to table
        table.registerType(IntType.getInst());
        table.registerType(FloatType.getInst());
        table.registerType(BoolType.getInst());
        table.registerType(VoidType.getInst());

        // Add mappings from token types to data types
        table.mapLiteralToType(TokType.INT_LITERAL, IntType.getInst());
        table.mapLiteralToType(TokType.FLOAT_LITERAL, FloatType.getInst());
        table.mapLiteralToType(TokType.BOOL_LITERAL, BoolType.getInst());
        return table;
    }

    /**
     * Adds a new data type to the table.
     *
     * @param dtype TypeInfo object that carries type data.
     */
    public void registerType(TypeInfo dtype) {
        strToType.put(dtype.getId(), dtype);
    }

    /**
     * Gets the data type associated with the given identifier.
     *
     * @param id identifier of the type.
     * @return a TypeInfo object associated with the given identifier.
     */
    public TypeInfo getType(String id) {
        return strToType.get(id);
    }

    /**
     * Maps a token type, which is also a literal type, to a data type.
     *
     * @param literalType the token type to be mapped from.
     * @param dtype       the data type to be mapped to.
     */
    private void mapLiteralToType(TokType literalType, TypeInfo dtype) {
        literalToType.put(literalType, dtype);
    }

    /**
     * Gets the data type associated with the given token type as the literal type.
     *
     * @param tokType the input token type.
     * @return the data type mapped from the given token type or null if it does not exist.
     */
    public TypeInfo getType(TokType tokType) {
        return literalToType.get(tokType);
    }
}
