package ast;

import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node for function definition
public class FunDefASTNode extends ASTNode {
    private IdASTNode idNode;
    private FunSignASTNode signNode;
    private ScopeASTNode bodyNode;

    public FunDefASTNode(Tok tok, TypeInfo retDtype) {
        super(tok, new SrcRange(tok.getSrcRange()), ASTNodeType.FUN_DEF, retDtype, false);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
    }

    public FunSignASTNode getSignNode() {
        return signNode;
    }

    public void setSignNode(FunSignASTNode signNode) {
        this.signNode = signNode;
    }

    public ScopeASTNode getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(ScopeASTNode bodyNode) {
        this.bodyNode = bodyNode;
        srcRange.setEndPos(bodyNode.srcRange.getEndPos());
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunDef(this);
    }
}
