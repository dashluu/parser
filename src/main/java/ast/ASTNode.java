package ast;

import toks.Tok;
import types.TypeInfo;

public abstract class ASTNode {
    protected Tok tok;
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

    public void setTok(Tok tok) {
        this.tok = tok;
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

    public String toJsonStr() {
        return "\"Node type\":\"" + (nodeType == null ? "null" : nodeType) + "\"" +
                ",\"Value\":\"" + (tok == null ? "null" : tok.getVal()) + "\"" +
                ",\"Data type\":\"" + (dtype == null ? "null" : dtype.id()) + "\"" +
                ",\"Size\":\"" + (dtype == null ? "0" : dtype.size()) + "\"";
    }

    public abstract void accept(IASTVisitor visitor);
}
