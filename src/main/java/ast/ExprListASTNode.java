package ast;

import types.TypeInfo;

public class ExprListASTNode extends MultichildASTNode {
    public ExprListASTNode(TypeInfo dtype) {
        super(ASTNodeType.EXPR_LIST, dtype, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitExprList(this);
    }
}
