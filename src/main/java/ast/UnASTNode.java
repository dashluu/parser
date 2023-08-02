package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node with data type and one child
public abstract class UnASTNode extends ASTNode {
    protected ASTNode child;

    public UnASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype, boolean valFlag) {
        super(tok, null, nodeType, dtype, valFlag);
    }

    public ASTNode getChild() {
        return child;
    }

    public void setChild(ASTNode child) {
        this.child = child;
    }

    public void updateSrcRange() {
        if (child != null) {
            SrcPos startPos = tok.getSrcRange().getStartPos();
            SrcPos endPos = child.getSrcRange().getEndPos();
            srcRange = new SrcRange(startPos, endPos);
        }
    }
}
