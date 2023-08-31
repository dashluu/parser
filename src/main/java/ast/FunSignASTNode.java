package ast;

import toks.SrcRange;
import types.TypeInfo;

public class FunSignASTNode extends ASTNode {
    private ParamListASTNode paramListNode;
    private DtypeASTNode retDtypeNode;

    public FunSignASTNode(TypeInfo dtype) {
        // data type is the same as the function's return type
        super(null, new SrcRange(), ASTNodeType.FUN_SIGN, dtype);
    }

    public ParamListASTNode getParamListNode() {
        return paramListNode;
    }

    public void setParamListNode(ParamListASTNode paramListNode) {
        this.paramListNode = paramListNode;
        srcRange.setStartPos(paramListNode.srcRange.getStartPos());
    }

    public DtypeASTNode getRetDtypeNode() {
        return retDtypeNode;
    }

    public void setRetDtypeNode(DtypeASTNode retDtypeNode) {
        this.retDtypeNode = retDtypeNode;
        srcRange.setEndPos(retDtypeNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunSign(this);
    }
}
