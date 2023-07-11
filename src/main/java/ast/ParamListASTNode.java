package ast;

public class ParamListASTNode extends KnaryASTNode {
    public ParamListASTNode() {
        super(null, ASTNodeType.PARAM_LIST, null);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitParamList(this);
    }
}
