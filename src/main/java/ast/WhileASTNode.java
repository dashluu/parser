package ast;

import toks.Tok;

public class WhileASTNode extends BranchNode {
    public WhileASTNode(Tok tok) {
        super(tok, ASTNodeType.WHILE);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitWhile(this);
    }
}
