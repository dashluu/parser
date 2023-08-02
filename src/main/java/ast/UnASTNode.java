package ast;

import toks.Tok;
import types.TypeInfo;

// Node with data type and one child
public abstract class UnASTNode extends ASTNode {
    protected ASTNode child;

    public UnASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype, boolean valFlag) {
        super(tok, tok.getSrcRange(), nodeType, dtype, valFlag);
    }

    public ASTNode getChild() {
        return child;
    }

    public void setChild(ASTNode child) {
        this.child = child;
        srcRange.setEndPos(child.srcRange.getEndPos());
    }
}
