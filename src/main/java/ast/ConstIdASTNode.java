package ast;

import toks.Tok;
import types.TypeInfo;

public class ConstIdASTNode extends ASTNode {
    public ConstIdASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.CONST_ID, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitConstId(this);
    }
}
