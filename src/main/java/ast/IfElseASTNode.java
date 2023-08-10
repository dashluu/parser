package ast;

public class IfElseASTNode extends MultichildASTNode {
    public IfElseASTNode() {
        super(ASTNodeType.IF_ELSE, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIfElse(this);
    }
}
