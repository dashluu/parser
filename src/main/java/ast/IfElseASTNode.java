package ast;

import toks.SrcRange;
import toks.Tok;

public class IfElseASTNode extends KnaryASTNode {
    public IfElseASTNode(SrcRange srcRange) {
        super(null, srcRange, ASTNodeType.IF_ELSE, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitIfElse(this);
    }
}
