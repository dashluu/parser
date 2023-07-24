package ast;

import toks.Tok;

public class ContASTNode extends ASTNode {
    public ContASTNode(Tok tok) {
        super(tok, ASTNodeType.CONT, null);
    }
}
