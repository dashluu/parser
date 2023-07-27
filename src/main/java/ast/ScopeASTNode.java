package ast;

// Node for code scopes
public class ScopeASTNode extends KnaryASTNode {
    public ScopeASTNode() {
        super(null, ASTNodeType.SCOPE, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitScope(this);
    }
}
