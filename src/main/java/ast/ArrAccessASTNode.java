package ast;

import toks.SrcPos;
import toks.SrcRange;
import types.TypeInfo;

public class ArrAccessASTNode extends ASTNode {
    private IdASTNode idNode;
    private ExprListASTNode indexListNode;

    public ArrAccessASTNode(TypeInfo dtype) {
        super(null, null, ASTNodeType.ARR_ACCESS, dtype, true);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
    }

    public ExprListASTNode getIndexListNode() {
        return indexListNode;
    }

    public void setIndexListNode(ExprListASTNode indexListNode) {
        this.indexListNode = indexListNode;
    }

    public void updateSrcRange() {
        SrcPos startPos = idNode.getSrcRange().getStartPos();
        SrcPos endPos = indexListNode.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrAccess(this);
    }
}
