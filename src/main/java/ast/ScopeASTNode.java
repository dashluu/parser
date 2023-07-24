package ast;

// Node for code scopes
public class ScopeASTNode extends KnaryASTNode {
    private boolean retFlag;

    public ScopeASTNode() {
        super(null, ASTNodeType.SCOPE, null);
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
        return visitor.visitScope(this);
    }
}
