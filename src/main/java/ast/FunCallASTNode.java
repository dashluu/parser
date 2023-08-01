package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class FunCallASTNode extends KnaryASTNode {
    public FunCallASTNode(Tok tok, SrcRange srcRange, TypeInfo dtype) {
        super(tok, srcRange, ASTNodeType.FUN_CALL, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunCall(this);
    }
}
