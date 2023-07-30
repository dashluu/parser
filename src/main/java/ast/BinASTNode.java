package ast;

import toks.Tok;
import types.TypeInfo;

// Node with data type and two children
public abstract class BinASTNode extends ASTNode {
    protected ASTNode left;
    protected ASTNode right;

    public BinASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype) {
        super(tok, nodeType, dtype);
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
}
