package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node with data type and two children
public abstract class BinASTNode extends ASTNode {
    protected ASTNode left;
    protected ASTNode right;

    public BinASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype, boolean valFlag) {
        super(tok, null, nodeType, dtype, valFlag);
        left = right = null;
    }

    public ASTNode getLeft() {
        return left;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public ASTNode getRight() {
        return right;
    }

    public void setRight(ASTNode right) {
        this.right = right;
    }

    public void updateSrcRange() {
        SrcPos startPos = left.getSrcRange().getStartPos();
        SrcPos endPos = right.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }
}
