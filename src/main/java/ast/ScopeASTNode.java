package ast;

import toks.SrcRange;

// Node for code scopes
public class ScopeASTNode extends ListASTNode {
    public ScopeASTNode(SrcRange srcRange) {
        super(srcRange, ASTNodeType.SCOPE, null, false);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitScope(this);
    }
}
