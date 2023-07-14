package ast;

import toks.Tok;

public class ElseASTNode extends KnaryASTNode {
    public ElseASTNode(Tok tok) {
        super(tok, ASTNodeType.ELSE, null);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitElse(this);
    }
}
