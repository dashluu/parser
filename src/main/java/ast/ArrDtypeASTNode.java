package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class ArrDtypeASTNode extends DtypeASTNode {
    public ArrDtypeASTNode(Tok tok, SrcRange srcRange, ASTNodeType nodeType, TypeInfo dtype) {
        super(tok, srcRange, nodeType, dtype);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return null;
    }
}
