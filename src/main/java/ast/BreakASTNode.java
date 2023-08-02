package ast;

import toks.Tok;

public class BreakASTNode extends ASTNode {
    public BreakASTNode(Tok tok) {
        super(tok, tok.getSrcRange(), ASTNodeType.BREAK, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitBreak(this);
    }
}
