package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node with data type and two children
public abstract class BinASTNode extends ASTNode {
    protected ASTNode left;
    protected ASTNode right;

    public BinASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype, boolean valFlag) {
        super(tok, new SrcRange(tok.getSrcRange()), nodeType, dtype, valFlag);
    }

    public ASTNode getLeft() {
        return left;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
        srcRange.setStartPos(left.srcRange.getStartPos());
    }

    public ASTNode getRight() {
        return right;
    }

    public void setRight(ASTNode right) {
        this.right = right;
        srcRange.setEndPos(right.srcRange.getEndPos());
    }
}
