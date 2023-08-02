package ast;

import toks.SrcPos;
import toks.SrcRange;
import types.TypeInfo;

public class FunCallASTNode extends ASTNode {
    private IdASTNode idNode;
    private ExprListASTNode argListNode;

    public FunCallASTNode(TypeInfo dtype) {
        super(null, null, ASTNodeType.FUN_CALL, dtype, true);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
    }

    public ExprListASTNode getArgListNode() {
        return argListNode;
    }

    public void setArgListNode(ExprListASTNode argListNode) {
        this.argListNode = argListNode;
    }

    public void updateSrcRange() {
        SrcPos startPos = idNode.getSrcRange().getStartPos();
        SrcPos endPos = argListNode.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunCall(this);
    }
}
