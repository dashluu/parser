package ast;

import toks.Tok;

public abstract class BranchNode extends ASTNode {
    protected ASTNode condNode;
    protected ScopeASTNode bodyNode;

    public BranchNode(Tok tok, ASTNodeType nodeType) {
        super(tok, nodeType, null);
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

    @Override
    public String toJsonStr() {
        return super.toJsonStr() +
                ",\"Condition\":" + (condNode == null ? "\"null\"" : "{" + condNode.toJsonStr() + "}") +
                ",\"Body\":" + (bodyNode == null ? "\"null\"" : "{" + bodyNode.toJsonStr() + "}");
    }
}
