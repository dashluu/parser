package ast;

import toks.Tok;
import types.TypeInfo;

public class TypeAnnASTNode extends ASTNode {
    private ASTNode left;
    private ASTNode dtypeNode;

    public TypeAnnASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.TYPE_ANN, dtype);
    }

    public ASTNode getLeft() {
        return left;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public ASTNode getDtypeNode() {
        return dtypeNode;
    }

    public void setDtypeNode(ASTNode dtypeNode) {
        this.dtypeNode = dtypeNode;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitTypeAnn(this);
    }
}
