package operators;

import toks.TokType;

// Base class for operator compatibility
public abstract class OpCompat {
    protected final TokType id;
    protected final OpCompatType compatType;

    public OpCompat(TokType id, OpCompatType compatType) {
        this.id = id;
        this.compatType = compatType;
    }

    public TokType getId() {
        return id;
    }

    public OpCompatType getCompatType() {
        return compatType;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OpCompat opCompat)) {
            return false;
        }
        return id == opCompat.id && compatType == opCompat.compatType;
    }
}
