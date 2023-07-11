package ast;

import toks.Tok;
import types.TypeInfo;

public class ParamASTNode extends ASTNode {
    public ParamASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.PARAM, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitParam(this);
    }
}
