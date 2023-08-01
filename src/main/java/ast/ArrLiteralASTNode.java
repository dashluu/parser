package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class ArrLiteralASTNode extends KnaryASTNode {
    public ArrLiteralASTNode(SrcRange srcRange, TypeInfo dtype) {
        super(null, srcRange, ASTNodeType.ARR_LITERAL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrLiteral(this);
    }
}
