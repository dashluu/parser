package ast;

import toks.Tok;
import types.TypeInfo;

public class ArrLiteralASTNode extends KnaryASTNode {
    public ArrLiteralASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.ARR_LITERAL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrLiteral(this);
    }
}
