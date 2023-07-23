package ast;

import toks.Tok;
import types.TypeInfo;

public class FunCallASTNode extends KnaryASTNode {
    public FunCallASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.FUN_CALL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunCall(this);
    }
}
