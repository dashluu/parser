package ast;

import toks.Tok;

public class IfElseASTNode extends KnaryASTNode {
    public IfElseASTNode(Tok tok) {
        super(tok, ASTNodeType.IF_ELSE, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIfElse(this);
    }
}
