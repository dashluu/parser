package ast;

import toks.Tok;
import types.TypeInfo;

public class RetASTNode extends UnASTNode {
    public RetASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.RET, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitRet(this);
    }
}
