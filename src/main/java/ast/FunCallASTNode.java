package ast;

import toks.Tok;
import types.TypeInfo;

public class FunCallASTNode extends KnaryASTNode {
    private final long blockId;

    public FunCallASTNode(Tok tok, TypeInfo dtype, long blockId) {
        super(tok, ASTNodeType.FUN_CALL, dtype);
        this.blockId = blockId;
    }

    public long getBlockId() {
        return blockId;
    }

    @Override
    public String toJsonStr() {
        return super.toJsonStr() + ",\"Block ID\":\"" + blockId + "\"";
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitFunCall(this);
    }
}
