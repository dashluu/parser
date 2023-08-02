package ast;

public class ParamListASTNode extends ListASTNode {
    public ParamListASTNode() {
        super(ASTNodeType.PARAM_LIST, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamList(this);
    }
}
