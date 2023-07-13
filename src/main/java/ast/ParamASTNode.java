package ast;

import toks.Tok;
import types.TypeInfo;

public class ParamASTNode extends ASTNode {
    private final int label;

    public ParamASTNode(Tok tok, TypeInfo dtype, int label) {
        super(tok, ASTNodeType.PARAM, dtype);
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitParam(this);
    }
}
