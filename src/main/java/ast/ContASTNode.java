package ast;

import toks.Tok;

public class ContASTNode extends ASTNode {
    public ContASTNode(Tok tok) {
        super(tok, ASTNodeType.CONT, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitCont(this);
    }
}
