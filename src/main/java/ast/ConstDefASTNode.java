package ast;

import toks.Tok;
import types.TypeInfo;

public class ConstDefASTNode extends BinASTNode {
    public ConstDefASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.CONST_DEF, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitConstDef(this);
    }
}
