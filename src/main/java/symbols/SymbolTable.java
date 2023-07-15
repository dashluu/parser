package symbols;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, SymbolInfo> symbolMap = new HashMap<>();
    private final SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return parent;
    }

    /**
     * Adds a new symbol to the table.
     *
     * @param symbol the symbol to be registered.
     */
    public void registerSymbol(SymbolInfo symbol) {
        symbolMap.put(symbol.getId(), symbol);
    }

    /**
     * Finds the symbol associated with the given key by moving up the chain of symbol tables.
     *
     * @param id the string that identifies a symbol in one of the tables.
     * @return a symbol if one exists and null otherwise.
     */
    public SymbolInfo getClosureSymbol(String id) {
        SymbolTable table = this;
        SymbolInfo symbolInfo = null;
        while (table != null && symbolInfo == null) {
            symbolInfo = table.symbolMap.get(id);
            table = table.parent;
        }
        return symbolInfo;
    }

    /**
     * Finds the symbol associated with the given key in the current scope's symbol table only.
     *
     * @param id the string that identifies a symbol in the table.
     * @return a symbol if one exists and null otherwise.
     */
    public SymbolInfo getLocalSymbol(String id) {
        return symbolMap.get(id);
    }
}
