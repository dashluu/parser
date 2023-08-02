package ast;

import toks.Tok;
import types.TypeInfo;

public class IdASTNode extends ASTNode {
    private boolean mutable;

    public IdASTNode(Tok tok, TypeInfo dtype, boolean mutable) {
        super(tok, tok.getSrcRange(), ASTNodeType.ID, dtype, true);
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
        return visitor.visitId(this);
    }
}
