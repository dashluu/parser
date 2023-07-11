package ast;

import toks.Tok;
import types.TypeInfo;

// Node for function definition
public class FunDefASTNode extends ASTNode {
    private ParamListASTNode paramListNode;
    private ScopeASTNode bodyNode;

    public FunDefASTNode(Tok tok, TypeInfo returnDtype) {
        super(tok, ASTNodeType.FUN_DEF, returnDtype);
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
    public String toJsonStr() {
        return super.toJsonStr() +
                ",\"Params\":" + (paramListNode == null ? "\"null\"" : "{" + paramListNode.toJsonStr() + "}") +
                ",\"Body\":" + (bodyNode == null ? "\"null\"" : "{" + bodyNode.toJsonStr() + "}");
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitFunDef(this);
    }
}
