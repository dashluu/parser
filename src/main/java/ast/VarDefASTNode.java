package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

public class VarDefASTNode extends ASTNode {
    private VarDeclASTNode varDeclNode;
    private ASTNode exprNode;

    public VarDefASTNode(Tok tok, TypeInfo dtype) {
        super(tok, new SrcRange(tok.getSrcRange()), ASTNodeType.VAR_DEF, dtype);
    }

    public VarDeclASTNode getVarDeclNode() {
        return varDeclNode;
    }

    public void setVarDeclNode(VarDeclASTNode varDeclNode) {
        this.varDeclNode = varDeclNode;
        srcRange.setStartPos(varDeclNode.srcRange.getStartPos());
    }

    public ASTNode getExprNode() {
        return exprNode;
    }

    public void setExprNode(ASTNode exprNode) {
        this.exprNode = exprNode;
        srcRange.setEndPos(exprNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitVarDef(this);
    }
}
