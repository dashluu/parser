package ast;

import toks.Tok;
import types.TypeInfo;

public class IdASTNode extends ASTNode {
    private IdType idType;

    public IdASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.ID, dtype);
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    @Override
    public void accept(IASTVisitor visitor) {

    }
}
