package ast;

import toks.Tok;
import types.TypeInfo;

// Node for function definition
public class FunDefASTNode extends ASTNode {
    private ParamListASTNode paramListNode;
    private ScopeASTNode bodyNode;

    public FunDefASTNode(Tok tok, TypeInfo retDtype) {
        super(tok, ASTNodeType.FUN_DEF, retDtype);
        paramListNode = null;
        bodyNode = null;
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

    @Override
    public ASTNode accept(IASTVisitor visitor) {
        return visitor.visitFunDef(this);
    }
}
