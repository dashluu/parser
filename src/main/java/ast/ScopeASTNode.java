package ast;

import toks.SrcRange;

// Node for code scopes
public class ScopeASTNode extends KnaryASTNode {
    public ScopeASTNode(SrcRange srcRange) {
        super(null, srcRange, ASTNodeType.SCOPE, null);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitScope(this);
    }
}
