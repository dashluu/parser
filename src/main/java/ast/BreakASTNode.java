package ast;

import toks.Tok;

public class BreakASTNode extends ASTNode {
    public BreakASTNode(Tok tok) {
        super(tok, ASTNodeType.BREAK, null);
    }
}
