package ast;

import toks.Tok;
import types.TypeInfo;

public class FunCallASTNode extends KnaryASTNode {
    public FunCallASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.FUN_CALL, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitFunCall(this);
    }
}
