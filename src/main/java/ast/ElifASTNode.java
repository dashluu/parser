package ast;

import toks.Tok;

public class ElifASTNode extends BranchNode {
    public ElifASTNode(Tok tok) {
        super(tok, ASTNodeType.ELIF);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitElif(this);
    }
}
