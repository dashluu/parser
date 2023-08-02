package ast;

import toks.Tok;
import types.TypeInfo;

public class RetASTNode extends UnASTNode {
    public RetASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.RET, dtype, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitRet(this);
    }
}
