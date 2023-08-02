package ast;

import toks.Tok;
import types.TypeInfo;

public class DtypeASTNode extends ASTNode {
    public DtypeASTNode(Tok tok, TypeInfo dtype) {
        // The data type node cannot be a value for an operator except for the type conversion operator
        // This will be checked by the semantics checker
        super(tok, tok.getSrcRange(), ASTNodeType.DTYPE, dtype, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitDtype(this);
    }
}
