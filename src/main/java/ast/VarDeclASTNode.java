package ast;

import toks.Tok;
import types.TypeInfo;

public class VarDeclASTNode extends ASTNode {
    public VarDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.VAR_DECL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitVarDecl(this);
    }
}
