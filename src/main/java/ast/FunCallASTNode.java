package ast;

import toks.Tok;
import types.TypeInfo;

public class FunCallASTNode extends KnaryASTNode {
    private final int label;

    public FunCallASTNode(Tok tok, TypeInfo dtype, int label) {
        super(tok, ASTNodeType.FUN_CALL, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitFunCall(this);
    }
}
