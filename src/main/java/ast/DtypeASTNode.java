package ast;

import toks.Tok;
import types.TypeInfo;

public class DtypeASTNode extends ASTNode {
    public DtypeASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.DTYPE, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitDtype(this);
    }
}
