package ast;

import types.TypeInfo;

public class GenericDtypeASTNode extends MultichildASTNode {
    public GenericDtypeASTNode(TypeInfo dtype) {
        super(ASTNodeType.GENERIC_DTYPE, dtype, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitGenericDtype(this);
    }
}
