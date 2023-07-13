package ast;

import toks.Tok;
import types.TypeInfo;

public class VarIdASTNode extends ASTNode {
    private final int label;

    public VarIdASTNode(Tok tok, TypeInfo dtype, int label) {
        super(tok, ASTNodeType.VAR_ID, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitVarId(this);
    }
}
