package ast;

import types.TypeInfo;

public class ArrLiteralASTNode extends KnaryASTNode {
    public ArrLiteralASTNode(TypeInfo dtype) {
        super(null, ASTNodeType.ARR_LITERAL, dtype);
    }
}
