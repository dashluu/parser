package ast;

import toks.Tok;
import types.TypeInfo;

public class VarDeclASTNode extends ASTNode {
    private boolean mutable;

    public VarDeclASTNode(Tok tok, TypeInfo dtype, boolean mutable) {
        super(tok, ASTNodeType.VAR_DECL, dtype);
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
        return visitor.visitVarDecl(this);
    }
}
