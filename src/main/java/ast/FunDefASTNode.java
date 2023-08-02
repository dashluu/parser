package ast;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import types.TypeInfo;

// Node for function definition
public class FunDefASTNode extends ASTNode {
    private IdASTNode idNode;
    private ParamListASTNode paramListNode;
    private ScopeASTNode bodyNode;

    public FunDefASTNode(Tok tok, TypeInfo retDtype) {
        super(tok, null, ASTNodeType.FUN_DEF, retDtype, false);
    }

    public IdASTNode getIdNode() {
        return idNode;
    }

    public void setIdNode(IdASTNode idNode) {
        this.idNode = idNode;
    }

    public ParamListASTNode getParamListNode() {
        return paramListNode;
    }

    public void setParamListNode(ParamListASTNode paramListNode) {
        this.paramListNode = paramListNode;
    }

    public ScopeASTNode getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(ScopeASTNode bodyNode) {
        this.bodyNode = bodyNode;
    }

    public void updateSrcRange() {
        SrcPos startPos = tok.getSrcRange().getStartPos();
        SrcPos endPos = bodyNode.getSrcRange().getEndPos();
        srcRange = new SrcRange(startPos, endPos);
    }

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunDef(this);
    }
}
