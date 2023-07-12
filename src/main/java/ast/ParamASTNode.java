package ast;

import toks.Tok;
import types.TypeInfo;

public class ParamASTNode extends ASTNode {
    private final long memId;

    public ParamASTNode(Tok tok, TypeInfo dtype, long memId) {
        super(tok, ASTNodeType.PARAM, dtype);
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
        visitor.visitParam(this);
    }
}
