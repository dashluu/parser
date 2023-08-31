package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public abstract class DtypeASTNode extends ASTNode {
    public DtypeASTNode(Tok tok, SrcRange srcRange, ASTNodeType nodeType, TypeInfo dtype) {
        // The data type node cannot be a value for an operator except for the type conversion operator
        // This will be checked by the semantic checker
        super(tok, srcRange, nodeType, dtype);
    }
}
