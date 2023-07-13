package ast;

import toks.Tok;

public class ElifASTNode extends BranchNode {
    public ElifASTNode(Tok tok, int label) {
        super(tok, ASTNodeType.ELIF, label);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitElif(this);
    }
}
