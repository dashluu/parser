package ast;

import toks.Tok;

public class ContASTNode extends ASTNode {
    public ContASTNode(Tok tok) {
        super(tok, tok.getSrcRange(), ASTNodeType.CONT, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitCont(this);
    }
}
