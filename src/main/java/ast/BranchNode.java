package ast;

import toks.Tok;

public abstract class BranchNode extends ASTNode {
    protected ASTNode condNode;
    protected ScopeASTNode bodyNode;
    private boolean retFlag;

    public BranchNode(Tok tok, ASTNodeType nodeType) {
        super(tok, nodeType, null);
        retFlag = false;
    }

    public ASTNode getCondNode() {
        return condNode;
    }

    public void setCondNode(ASTNode condNode) {
        this.condNode = condNode;
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
}
