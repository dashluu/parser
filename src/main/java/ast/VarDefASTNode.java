package ast;

import toks.Tok;
import types.TypeInfo;

public class VarDefASTNode extends BinASTNode {
    private boolean mutable;

    public VarDefASTNode(Tok tok, TypeInfo dtype, boolean mutable) {
        super(tok, ASTNodeType.DEF, dtype, false);
        this.mutable = mutable;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitVarDef(this);
    }
}
