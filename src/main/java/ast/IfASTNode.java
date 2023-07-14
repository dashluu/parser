package ast;

import toks.Tok;

public class IfASTNode extends BranchNode {
    public IfASTNode(Tok tok) {
        super(tok, ASTNodeType.IF);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitIf(this);
    }
}
