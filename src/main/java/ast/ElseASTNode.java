package ast;

import toks.Tok;

public class ElseASTNode extends ASTNode {
    private ScopeASTNode bodyNode;
    private boolean retFlag;

    public ElseASTNode(Tok tok) {
        super(tok, ASTNodeType.ELSE, null);
        retFlag = false;
    }

    public ScopeASTNode getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(ScopeASTNode bodyNode) {
        this.bodyNode = bodyNode;
    }

    public boolean getRetFlag() {
        return retFlag;
    }

    public void setRetFlag(boolean retFlag) {
        this.retFlag = retFlag;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitElse(this);
    }
}
