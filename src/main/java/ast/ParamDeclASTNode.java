package ast;

import toks.SrcRange;
import types.TypeInfo;

public class ParamDeclASTNode extends ASTNode {
    private IdASTNode idNode;
    private DtypeASTNode dtypeNode;

    public ParamDeclASTNode(TypeInfo dtype) {
        super(null, new SrcRange(), ASTNodeType.PARAM_DECL, dtype);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
        srcRange.setStartPos(idNode.srcRange.getStartPos());
    }

    public DtypeASTNode getDtypeNode() {
        return dtypeNode;
    }

    public void setDtypeNode(DtypeASTNode dtypeNode) {
        this.dtypeNode = dtypeNode;
        srcRange.setEndPos(dtypeNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitParamDecl(this);
    }
}
