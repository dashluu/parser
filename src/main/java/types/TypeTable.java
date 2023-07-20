package types;

import toks.TokType;

import java.util.HashMap;

public class TypeTable {
    public static final TypeInfo INT = new TypeInfo("Int", 4);
    public static final TypeInfo FLOAT = new TypeInfo("Float", 4);
    public static final TypeInfo BOOL = new TypeInfo("Bool", 1);
    public static final TypeInfo VOID = new TypeInfo("Void", 4);
    // String-to-type map
    private final HashMap<String, TypeInfo> STR_TO_TYPE = new HashMap<>();
    private final HashMap<TokType, TypeInfo> LITERAL_TO_TYPE = new HashMap<>();
    // Type conversion map
    private static final HashMap<TypeConv, TypeConv> TYPE_CONV_MAP = new HashMap<>();
    private static final TypeTable INST = new TypeTable();
    private static boolean init = false;

    private TypeTable() {
    }

    /**
     * Initializes the only instance of TypeTable if it has not been initialized and then returns it.
     *
     * @return a TypeTable object.
     */
    public static TypeTable getInst() {
        if (!init) {
            // Add types to table
            INST.registerType(INT);
            INST.registerType(FLOAT);
            INST.registerType(BOOL);
            INST.registerType(VOID);

            // Add mappings from token types to data types
            INST.mapLiteralToType(TokType.INT_LITERAL, INT);
            INST.mapLiteralToType(TokType.FLOAT_LITERAL, FLOAT);
            INST.mapLiteralToType(TokType.BOOL_LITERAL, BOOL);

            // Add pairs of types that can be converted from one to another
            INST.registerTypeConv(new TypeConv(INT, INT, true));
            INST.registerTypeConv(new TypeConv(INT, FLOAT, true));
            INST.registerTypeConv(new TypeConv(FLOAT, INT, true));
            INST.registerTypeConv(new TypeConv(FLOAT, FLOAT, true));

            init = true;
        }
        return INST;
    }

    /**
     * Adds a new data type to the table.
     *
     * @param dtype TypeInfo object that carries type data.
     */
    public void registerType(TypeInfo dtype) {
        STR_TO_TYPE.put(dtype.id(), dtype);
    }

    /**
     * Gets the data type associated with the given id.
     *
     * @param id id of the type.
     * @return a TypeInfo object associated with the given id.
     */
    public TypeInfo getType(String id) {
        return STR_TO_TYPE.get(id);
    }

    /**
     * Maps a token type, which is also a literal type, to a data type.
     *
     * @param literalType the token type to be mapped from.
     * @param dtype       the data type to be mapped to.
     */
    private void mapLiteralToType(TokType literalType, TypeInfo dtype) {
        LITERAL_TO_TYPE.put(literalType, dtype);
    }

    /**
     * Gets the data type associated with the given token type as the literal type.
     *
     * @param tokType the input token type.
     * @return the data type mapped from the given token type or null if it does not exist.
     */
    public TypeInfo getType(TokType tokType) {
        return LITERAL_TO_TYPE.get(tokType);
    }

    /**
     * Adds a new type conversion object to the type conversion table.
     *
     * @param typeConv TypeConv object that contains type conversion information.
     */
    private void registerTypeConv(TypeConv typeConv) {
        TYPE_CONV_MAP.put(typeConv, typeConv);
    }

    /**
     * Gets a type conversion object using the source and destination data type.
     *
     * @param srcDtype  the source data type.
     * @param destDtype the destination data type.
     * @return a TypeConv object if the two types are convertible and null otherwise.
     */
    public TypeConv getTypeConv(TypeInfo srcDtype, TypeInfo destDtype) {
        // dummy object
        TypeConv typeConv = new TypeConv(srcDtype, destDtype, false);
        return TYPE_CONV_MAP.get(typeConv);
    }
}
