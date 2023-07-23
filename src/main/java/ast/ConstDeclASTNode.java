package ast;

import toks.Tok;
import types.TypeInfo;

public class ConstDeclASTNode extends ASTNode {
    public ConstDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.CONST_DECL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitConstDecl(this);
    }
}
