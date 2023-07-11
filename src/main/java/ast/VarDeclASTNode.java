package ast;

import toks.Tok;
import types.TypeInfo;

public class VarDeclASTNode extends UnASTNode {
    public VarDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.VAR_DECL, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitVarDecl(this);
    }
}
