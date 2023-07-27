package ast;

import toks.Tok;

public class IfElseASTNode extends KnaryASTNode {
    private boolean retFlag;

    public IfElseASTNode(Tok tok) {
        super(tok, ASTNodeType.IF_ELSE, null);
        retFlag = false;
    }

    public boolean getRetFlag() {
        return retFlag;
    }

    public void setRetFlag(boolean retFlag) {
        this.retFlag = retFlag;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIfElse(this);
    }
}
