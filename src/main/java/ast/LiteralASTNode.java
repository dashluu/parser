package ast;

import toks.Tok;
import types.TypeInfo;

public class LiteralASTNode extends ASTNode {
    public LiteralASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.LITERAL, dtype);
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitLiteral(this);
    }
}
