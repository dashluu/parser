package ast;

import toks.Tok;
import types.TypeInfo;

public class ConstDeclASTNode extends UnASTNode {
    public ConstDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.CONST_DECL, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitConstDecl(this);
    }
}
