package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;

public abstract class BranchNode extends ASTNode {
    protected ASTNode condNode;
    protected ScopeASTNode bodyNode;

    public BranchNode(Tok tok, ASTNodeType nodeType) {
        super(tok, null, nodeType, null, false);
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

    public void updateSrcRange() {
        SrcPos startPos = tok.getSrcRange().getStartPos();
        SrcPos endPos = bodyNode.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }
}
