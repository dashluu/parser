package ast;

import toks.Tok;
import types.TypeInfo;

public class TypeAnnASTNode extends ASTNode {
    private ASTNode left;
    private DtypeASTNode dtypeNode;

    public TypeAnnASTNode(Tok tok, TypeInfo dtype) {
        super(tok, ASTNodeType.TYPE_ANN, dtype);
    }

    public ASTNode getLeft() {
        return left;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public DtypeASTNode getDtypeNode() {
        return dtypeNode;
    }

    public void setDtypeNode(DtypeASTNode dtypeNode) {
        this.dtypeNode = dtypeNode;
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitTypeAnn(this);
    }

    @Override
    public String toJsonStr() {
        return super.toJsonStr() +
                ",\"Left\":" + (left == null ? "\"null\"" : "{" + left.toJsonStr() + "}") +
                ",\"Data type node\":" + (dtypeNode == null ? "\"null\"" : "{" + dtypeNode.toJsonStr() + "}");
    }
}
