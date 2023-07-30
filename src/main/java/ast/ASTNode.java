package ast;

import toks.Tok;
import types.TypeInfo;

public abstract class ASTNode {
    protected final Tok tok;
    protected final ASTNodeType nodeType;
    protected TypeInfo dtype;

    public ASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype) {
        this.tok = tok;
        this.nodeType = nodeType;
        this.dtype = dtype;
    }

    public Tok getTok() {
        return tok;
    }

    public ASTNodeType getNodeType() {
        return nodeType;
    }

    public TypeInfo getDtype() {
        return dtype;
    }

    public void setDtype(TypeInfo dtype) {
        this.dtype = dtype;
    }

    public abstract ASTNode accept(IASTVisitor visitor);
}
