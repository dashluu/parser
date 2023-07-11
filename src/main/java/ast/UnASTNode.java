package ast;

import toks.Tok;
import types.TypeInfo;

// Node with data type and one child
public abstract class UnASTNode extends ASTNode {
    protected ASTNode child;

    public UnASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype) {
        super(tok, nodeType, dtype);
        child = null;
    }

    public ASTNode getChild() {
        return child;
    }

    public void setChild(ASTNode child) {
        this.child = child;
    }

    @Override
    public String toJsonStr() {
        return super.toJsonStr() + ",\"Child\":" + (child == null ? "\"null\"" : "{" + child.toJsonStr() + "}");
    }
}
