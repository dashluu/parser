package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;

public class ElseASTNode extends ASTNode {
    private ScopeASTNode bodyNode;

    public ElseASTNode(Tok tok) {
        super(tok, null, ASTNodeType.ELSE, null, false);
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

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitElse(this);
    }
}
