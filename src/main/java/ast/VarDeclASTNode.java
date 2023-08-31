package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class VarDeclASTNode extends ASTNode {
    private IdASTNode idNode;
    private DtypeASTNode dtypeNode;

    public VarDeclASTNode(Tok tok, TypeInfo dtype) {
        super(tok, new SrcRange(tok.getSrcRange()), ASTNodeType.VAR_DECL, dtype);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
        srcRange.setEndPos(idNode.srcRange.getEndPos());
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
        return visitor.visitVarDecl(this);
    }
}
