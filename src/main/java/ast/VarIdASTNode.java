package ast;

import toks.Tok;
import types.TypeInfo;

public class VarIdASTNode extends ASTNode {
    private final long memId;

    public VarIdASTNode(Tok tok, TypeInfo dtype, long memId) {
        super(tok, ASTNodeType.VAR_ID, dtype);
        this.memId = memId;
    }

    public long getMemId() {
        return memId;
    }

    @Override
    public String toJsonStr() {
        return super.toJsonStr() + ",\"Memory ID\":\"" + memId + "\"";
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitVarId(this);
    }
}
