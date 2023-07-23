package ast;

import toks.Tok;

public class WhileASTNode extends BranchNode {
    public WhileASTNode(Tok tok) {
        super(tok, ASTNodeType.WHILE);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitWhile(this);
    }
}
