package ast;

import toks.Tok;

public class ElifASTNode extends BranchNode {
    public ElifASTNode(Tok tok) {
        super(tok, ASTNodeType.ELIF);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitElif(this);
    }
}
