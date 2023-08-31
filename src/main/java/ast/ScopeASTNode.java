package ast;

// Node for code scopes
public class ScopeASTNode extends MultichildASTNode {
    public ScopeASTNode() {
        super(ASTNodeType.SCOPE, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitScope(this);
    }
}
