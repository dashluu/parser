package ast;

import toks.Tok;
import types.TypeInfo;

public class VarDefASTNode extends BinASTNode {
    public VarDefASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.VAR_DEF, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitVarDef(this);
    }
}
