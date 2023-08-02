package ast;

import toks.SrcRange;
import types.TypeInfo;

public class ArrAccessASTNode extends ASTNode {
    private IdASTNode idNode;
    private ExprListASTNode indexListNode;

    public ArrAccessASTNode(TypeInfo dtype) {
        super(null, new SrcRange(), ASTNodeType.ARR_ACCESS, dtype, true);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
        srcRange.setStartPos(idNode.srcRange.getStartPos());
    }

    public ExprListASTNode getIndexListNode() {
        return indexListNode;
    }

    public void setIndexListNode(ExprListASTNode indexListNode) {
        this.indexListNode = indexListNode;
        srcRange.setEndPos(indexListNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrAccess(this);
    }
}
