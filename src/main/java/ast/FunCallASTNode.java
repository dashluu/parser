package ast;

import toks.SrcRange;
import types.TypeInfo;

public class FunCallASTNode extends ASTNode {
    private IdASTNode idNode;
    private ExprListASTNode argListNode;

    public FunCallASTNode(TypeInfo dtype) {
        super(null, new SrcRange(), ASTNodeType.FUN_CALL, dtype, true);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
        srcRange.setStartPos(idNode.srcRange.getStartPos());
    }

    public ExprListASTNode getArgListNode() {
        return argListNode;
    }

    public void setArgListNode(ExprListASTNode argListNode) {
        this.argListNode = argListNode;
        srcRange.setEndPos(argListNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunCall(this);
    }
}
