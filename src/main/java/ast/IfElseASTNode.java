package ast;

import toks.SrcPos;
import toks.SrcRange;

public class IfElseASTNode extends ListASTNode {
    public IfElseASTNode(SrcRange srcRange) {
        super(srcRange, ASTNodeType.IF_ELSE, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIfElse(this);
    }
}
