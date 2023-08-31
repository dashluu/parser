package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node with data type and one child
public abstract class UnASTNode extends ASTNode {
    protected ASTNode exprNode;

    public UnASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype) {
        super(tok, new SrcRange(tok.getSrcRange()), nodeType, dtype);
    }

    public ASTNode getExprNode() {
        return exprNode;
    }

    public void setExprNode(ASTNode exprNode) {
        this.exprNode = exprNode;
        srcRange.setEndPos(exprNode.srcRange.getEndPos());
    }
}
