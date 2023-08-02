package ast;

// Node for code scopes
public class ScopeASTNode extends ListASTNode {
    public ScopeASTNode() {
        super(ASTNodeType.SCOPE, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitScope(this);
    }
}
