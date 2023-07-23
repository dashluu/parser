package ast;

public class ParamListASTNode extends KnaryASTNode {
    public ParamListASTNode() {
        super(null, ASTNodeType.PARAM_LIST, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamList(this);
    }
}
