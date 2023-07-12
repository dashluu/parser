package ast;

import toks.Tok;
import types.TypeInfo;

public class ParamDeclASTNode extends UnASTNode {
    public ParamDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.PARAM_DECL, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitParamDecl(this);
    }
}
