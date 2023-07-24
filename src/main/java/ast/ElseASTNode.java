package ast;

import toks.Tok;

public class ElseASTNode extends ASTNode {
    private ScopeASTNode bodyNode;

    public ElseASTNode(Tok tok) {
        super(tok, ASTNodeType.ELSE, null);
    }

    public ScopeASTNode getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(ScopeASTNode bodyNode) {
        this.bodyNode = bodyNode;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitElse(this);
    }
}
