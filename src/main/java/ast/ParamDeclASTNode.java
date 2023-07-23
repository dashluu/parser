package ast;

import toks.Tok;
import types.TypeInfo;

public class ParamDeclASTNode extends ASTNode {
    public ParamDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.PARAM_DECL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamDecl(this);
    }
}
