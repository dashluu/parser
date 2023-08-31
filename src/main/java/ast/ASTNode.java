package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public abstract class ASTNode {
    protected Tok tok;
    protected SrcRange srcRange;
    protected final ASTNodeType nodeType;
    // The type of data held inside the node
    protected TypeInfo dtype;

    public ASTNode(Tok tok, SrcRange srcRange, ASTNodeType nodeType, TypeInfo dtype) {
        this.tok = tok;
        this.srcRange = srcRange;
        this.nodeType = nodeType;
        this.dtype = dtype;
    }

    public Tok getTok() {
        return tok;
    }

    public void setTok(Tok tok) {
        this.tok = tok;
    }

    public SrcRange getSrcRange() {
        return srcRange;
    }

    public void setSrcRange(SrcRange srcRange) {
        this.srcRange = srcRange;
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
