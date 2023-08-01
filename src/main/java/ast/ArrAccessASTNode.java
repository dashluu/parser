package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class ArrAccessASTNode extends KnaryASTNode {
    private boolean mutable;

    public ArrAccessASTNode(Tok tok, SrcRange srcRange, TypeInfo dtype, boolean mutable) {
        super(tok, srcRange, ASTNodeType.ARR_ACCESS, dtype);
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
        return visitor.visitArrAccess(this);
    }
}
