package ast;

import toks.Tok;
import types.TypeInfo;

public class DtypeASTNode extends ASTNode {
    public DtypeASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.DTYPE, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitDtype(this);
    }
}
