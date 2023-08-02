package ast;

import toks.SrcRange;
import types.TypeInfo;

public class ExprListASTNode extends ListASTNode {
    public ExprListASTNode(SrcRange srcRange, TypeInfo dtype) {
        super(srcRange, ASTNodeType.EXPR_LIST, dtype, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitExprList(this);
    }
}
