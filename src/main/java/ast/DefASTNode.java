package ast;

import toks.Tok;
import types.TypeInfo;

public class DefASTNode extends BinASTNode {
    public DefASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.DEF, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitDef(this);
    }
}
