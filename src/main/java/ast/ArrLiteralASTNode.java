package ast;

import toks.SrcRange;
import types.TypeInfo;

public class ArrLiteralASTNode extends ListASTNode {
    public ArrLiteralASTNode(SrcRange srcRange, TypeInfo dtype) {
        super(srcRange, ASTNodeType.ARR_LITERAL, dtype, true);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrLiteral(this);
    }
}
