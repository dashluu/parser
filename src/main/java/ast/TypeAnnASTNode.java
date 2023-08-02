package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class TypeAnnASTNode extends ASTNode {
    private ASTNode left;
    private ASTNode dtypeNode;

    public TypeAnnASTNode(Tok tok, TypeInfo dtype) {
        super(tok, tok.getSrcRange(), ASTNodeType.TYPE_ANN, dtype, false);
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

    public void updateSrcRange() {
        SrcPos startPos = left.getSrcRange().getStartPos();
        SrcPos endPos = dtypeNode.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitTypeAnn(this);
    }
}
