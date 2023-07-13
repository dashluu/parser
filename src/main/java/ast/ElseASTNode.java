package ast;

import toks.Tok;

public class ElseASTNode extends BranchNode {
    public ElseASTNode(Tok tok, int label) {
        super(tok, ASTNodeType.ELSE, label);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitElse(this);
    }
}
