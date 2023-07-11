package ast;

import toks.Tok;
import types.TypeInfo;

public class TypeConvASTNode extends UnASTNode {
    public TypeConvASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.TYPE_CONV, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitTypeConv(this);
    }
}
