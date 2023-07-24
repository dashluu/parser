package ast;

import toks.Tok;
import types.TypeInfo;

public class ASTNode {
    protected Tok tok;
    protected ASTNodeType nodeType;
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

    public void setNodeType(ASTNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public TypeInfo getDtype() {
        return dtype;
    }

    public void setDtype(TypeInfo dtype) {
        this.dtype = dtype;
    }

    public ASTNode accept(IASTVisitor visitor) {
        return switch (nodeType) {
            case VAR_ID -> visitor.visitVarId(this);
            case CONST_ID -> visitor.visitConstId(this);
            case PARAM -> visitor.visitParam(this);
            default -> visitor.visitDtype(this);
        };
    }
}
