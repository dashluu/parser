package ast;

import toks.Tok;
import types.TypeInfo;

public class ConstIdASTNode extends ASTNode {
    private final int label;

    public ConstIdASTNode(Tok tok, TypeInfo dtype, int label) {
        super(tok, ASTNodeType.CONST_ID, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitConstId(this);
    }
}
