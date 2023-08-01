package ast;

import toks.SrcRange;

public class ParamListASTNode extends KnaryASTNode {
    public ParamListASTNode(SrcRange srcRange) {
        super(null, srcRange, ASTNodeType.PARAM_LIST, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamList(this);
    }
}
