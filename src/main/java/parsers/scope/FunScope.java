package parsers.scope;

import types.TypeInfo;

public class FunScope extends Scope {
    private TypeInfo retDtype;

    public FunScope(Scope parent, TypeInfo retDtype) {
        super(ScopeType.FUNCTION, parent);
        this.retDtype = retDtype;
    }

    public TypeInfo getRetDtype() {
        return retDtype;
    }

    public void setRetDtype(TypeInfo retDtype) {
        this.retDtype = retDtype;
    }
}
