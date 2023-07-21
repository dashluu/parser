package types;

import toks.TokType;

import java.util.HashMap;

public class TypeTable {
    public static final TypeInfo INT = new TypeInfo("Int", 4);
    public static final TypeInfo FLOAT = new TypeInfo("Float", 4);
    public static final TypeInfo BOOL = new TypeInfo("Bool", 1);
    public static final TypeInfo VOID = new TypeInfo("Void", 4);
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
        table.registerType(INT);
        table.registerType(FLOAT);
        table.registerType(BOOL);
        table.registerType(VOID);

        // Add mappings from token types to data types
        table.mapLiteralToType(TokType.INT_LITERAL, INT);
        table.mapLiteralToType(TokType.FLOAT_LITERAL, FLOAT);
        table.mapLiteralToType(TokType.BOOL_LITERAL, BOOL);
        return table;
    }

    /**
     * Adds a new data type to the table.
     *
     * @param dtype TypeInfo object that carries type data.
     */
    public void registerType(TypeInfo dtype) {
        strToType.put(dtype.id(), dtype);
    }

    /**
     * Gets the data type associated with the given id.
     *
     * @param id id of the type.
     * @return a TypeInfo object associated with the given id.
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
