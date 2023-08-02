package ast;

import toks.Tok;
import types.TypeInfo;

public class BinOpASTNode extends BinASTNode {
    public BinOpASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.BIN_OP, dtype, true);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitBinOp(this);
    }
}
