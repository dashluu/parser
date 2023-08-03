package ast;

import toks.SrcRange;
import toks.Tok;

public abstract class BranchNode extends ASTNode {
    protected ASTNode condNode;
    protected ScopeASTNode bodyNode;

    public BranchNode(Tok tok, ASTNodeType nodeType) {
        super(tok, new SrcRange(tok.getSrcRange()), nodeType, null, false);
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
        srcRange.setEndPos(bodyNode.srcRange.getEndPos());
    }
}
