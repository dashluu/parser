package symbols;

import types.TypeInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// A class for storing function information in the symbol table
// Implemented using the Iterator pattern
public class FunInfo extends SymbolInfo implements Iterable<TypeInfo> {
    private final List<TypeInfo> paramDtypes = new ArrayList<>();

    public FunInfo(String id, TypeInfo returnDtype) {
        super(id, SymbolType.FUN, returnDtype);
    }

    /**
     * Adds a parameter data type to the list.
     *
     * @param paramDtype the data type of a parameter.
     */
    public void addParamDtype(TypeInfo paramDtype) {
        paramDtypes.add(paramDtype);
    }

    /**
     * Counts the number of parameters of the function.
     *
     * @return an integer as the number of parameters.
     */
    public int countParams() {
        return paramDtypes.size();
    }

    /**
     * Gets an Iterator object to iterate through the list of parameter types.
     *
     * @return an Iterator object.
     */
    @Override
    public Iterator<TypeInfo> iterator() {
        return paramDtypes.iterator();
    }
}
