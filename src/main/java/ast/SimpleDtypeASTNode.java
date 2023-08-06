package ast;

import toks.Tok;
import types.TypeInfo;

public class SimpleDtypeASTNode extends DtypeASTNode {
    public SimpleDtypeASTNode(Tok tok, TypeInfo dtype) {
        super(tok, tok.getSrcRange(), ASTNodeType.SIMPLE_DTYPE, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitSimpleDtype(this);
    }
}
