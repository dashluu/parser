package ast;

import toks.Tok;
import types.TypeInfo;

public class LiteralASTNode extends ASTNode {
    public LiteralASTNode(Tok tok, TypeInfo dtype) {
        super(tok, tok.getSrcRange(), ASTNodeType.LITERAL, dtype, true);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitLiteral(this);
    }
}
