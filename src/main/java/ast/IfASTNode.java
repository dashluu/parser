package ast;

import toks.Tok;

public class IfASTNode extends BranchNode {
    public IfASTNode(Tok tok) {
        super(tok, ASTNodeType.IF);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIf(this);
    }
}
