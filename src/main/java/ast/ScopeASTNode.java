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
    public String toJsonStr() {
        return super.toJsonStr() + ",\"Return flag\":\"" + retFlag + "\"";
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitScope(this);
    }
}
