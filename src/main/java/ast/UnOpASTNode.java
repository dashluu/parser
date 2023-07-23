package ast;

import toks.Tok;
import types.TypeInfo;

public class UnOpASTNode extends UnASTNode {
    public UnOpASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.UN_OP, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitUnOp(this);
    }
}
