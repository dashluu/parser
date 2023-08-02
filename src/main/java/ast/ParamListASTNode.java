package ast;

import toks.SrcRange;

public class ParamListASTNode extends ListASTNode {
    public ParamListASTNode(SrcRange srcRange) {
        super(srcRange, ASTNodeType.PARAM_LIST, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamList(this);
    }
}
