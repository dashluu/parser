package ast;

import toks.SrcRange;
import types.TypeInfo;

public class ArrDtypeASTNode extends DtypeASTNode {
    private DtypeASTNode elmDtypeNode;

    public ArrDtypeASTNode(SrcRange srcRange, TypeInfo dtype) {
        super(null, srcRange, ASTNodeType.ARR_DTYPE, dtype);
    }

    public DtypeASTNode getElmDtypeNode() {
        return elmDtypeNode;
    }

    public void setElmDtypeNode(DtypeASTNode elmDtypeNode) {
        this.elmDtypeNode = elmDtypeNode;
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitArrDtype(this);
    }
}
