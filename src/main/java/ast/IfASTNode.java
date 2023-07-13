package ast;

import toks.Tok;

public class IfASTNode extends BranchNode {
    public IfASTNode(Tok tok, int label) {
        super(tok, ASTNodeType.IF, label);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitIf(this);
    }
}
