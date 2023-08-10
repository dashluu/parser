package ast;

import types.TypeInfo;

public class ArrLiteralASTNode extends MultichildASTNode {
    public ArrLiteralASTNode(TypeInfo dtype) {
        super(ASTNodeType.ARR_LITERAL, dtype, true);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrLiteral(this);
    }
}
