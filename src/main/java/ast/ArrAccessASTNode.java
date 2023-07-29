package ast;

import toks.Tok;
import types.TypeInfo;

public class ArrAccessASTNode extends KnaryASTNode {
    public ArrAccessASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.ARR_ACCESS, dtype);
    }
}
