package ast;

import toks.Tok;
import types.TypeInfo;

public class VarIdASTNode extends ASTNode {
    public VarIdASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.VAR_ID, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitVarId(this);
    }
}
