package ast;

import toks.Tok;

public class ElseASTNode extends ASTNode {
    private ScopeASTNode bodyNode;

    public ElseASTNode(Tok tok) {
        super(tok, tok.getSrcRange(), ASTNodeType.ELSE, null, false);
    }

    public ScopeASTNode getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(ScopeASTNode bodyNode) {
        this.bodyNode = bodyNode;
        srcRange.setEndPos(bodyNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitElse(this);
    }
}
