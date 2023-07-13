package ast;

import toks.Tok;

public abstract class BranchNode extends ASTNode {
    protected ASTNode condNode;
    protected ScopeASTNode bodyNode;
    protected final int label;

    public BranchNode(Tok tok, ASTNodeType nodeType, int label) {
        super(tok, nodeType, null);
        this.label = label;
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

    public int getLabel() {
        return label;
    }
}
