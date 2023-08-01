package ast;

import toks.Tok;
import types.TypeInfo;

import java.util.ListIterator;

public class JSONWalker implements IASTVisitor {
    private final StringBuilder jsonStrBuff = new StringBuilder();

    public String getJSON(ASTNode root) {
        jsonStrBuff.append("{");
        root.accept(this);
        jsonStrBuff.append("}");
        return jsonStrBuff.toString();
    }

    private void nodeToJSON(ASTNode node) {
        Tok tok = node.getTok();
        ASTNodeType nodeType = node.getNodeType();
        TypeInfo dtype = node.getDtype();
        jsonStrBuff.append("\"Node type\":\"").append(nodeType).append("\"")
                .append(",\"Tok\":\"").append(tok == null ? "null" : tok).append("\"")
                .append(",\"Data type\":\"").append(dtype == null ? "null" : dtype.getId()).append("\"");
    }

    private void unNodeToJSON(UnASTNode unNode) {
        nodeToJSON(unNode);
        ASTNode child = unNode.getChild();
        jsonStrBuff.append(",\"Left\":");
        if (child == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            child = child.accept(this);
            jsonStrBuff.append("}");
            unNode.setChild(child);
        }
    }

    private void binNodeToJSON(BinASTNode binNode) {
        nodeToJSON(binNode);
        ASTNode left = binNode.getLeft();
        ASTNode right = binNode.getRight();

        jsonStrBuff.append(",\"Left\":");
        if (left == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            left = left.accept(this);
            jsonStrBuff.append("}");
            binNode.setLeft(left);
        }

        jsonStrBuff.append(",\"Right\":");
        if (right == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            right = right.accept(this);
            jsonStrBuff.append("}");
            binNode.setRight(right);
        }
    }

    private void brNodeToJSON(BranchNode brNode) {
        nodeToJSON(brNode);
        ASTNode condNode = brNode.getCondNode();
        ScopeASTNode bodyNode = brNode.getBodyNode();

        jsonStrBuff.append(",\"Condition\":");
        if (condNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            condNode = condNode.accept(this);
            jsonStrBuff.append("}");
            brNode.setCondNode(condNode);
        }

        jsonStrBuff.append(",\"Body\":");
        if (bodyNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            bodyNode = (ScopeASTNode) bodyNode.accept(this);
            jsonStrBuff.append("}");
            brNode.setBodyNode(bodyNode);
        }
    }

    private void knaryNodeToJSON(KnaryASTNode knaryNode) {
        nodeToJSON(knaryNode);
        jsonStrBuff.append(",\"Source range\":\"").append(knaryNode.srcRange).append("\"");
        jsonStrBuff.append(",\"Children\":[");
        ListIterator<ASTNode> childrenIter = knaryNode.listIterator();
        ASTNode child;
        boolean firstChild = true;

        while (childrenIter.hasNext()) {
            if (!firstChild) {
                jsonStrBuff.append(",");
            }
            firstChild = false;
            jsonStrBuff.append("{");
            child = childrenIter.next().accept(this);
            childrenIter.set(child);
            jsonStrBuff.append("}");
        }

        jsonStrBuff.append("]");
    }

    @Override
    public ASTNode visitId(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitDtype(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitLiteral(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitVarDecl(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitVarDef(ASTNode node) {
        VarDefASTNode varDefNode = (VarDefASTNode) node;
        binNodeToJSON(varDefNode);
        return node;
    }

    @Override
    public ASTNode visitParamDecl(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitParamList(ASTNode node) {
        ParamListASTNode paramListNode = (ParamListASTNode) node;
        knaryNodeToJSON(paramListNode);
        return node;
    }

    @Override
    public ASTNode visitUnOp(ASTNode node) {
        UnOpASTNode unOpNode = (UnOpASTNode) node;
        unNodeToJSON(unOpNode);
        return node;
    }

    @Override
    public ASTNode visitBinOp(ASTNode node) {
        BinOpASTNode binOpNode = (BinOpASTNode) node;
        binNodeToJSON(binOpNode);
        return node;
    }

    @Override
    public ASTNode visitElse(ASTNode node) {
        nodeToJSON(node);
        ElseASTNode elseNode = (ElseASTNode) node;
        ScopeASTNode bodyNode = elseNode.getBodyNode();

        jsonStrBuff.append(",\"Body\":");
        if (bodyNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            bodyNode = (ScopeASTNode) bodyNode.accept(this);
            jsonStrBuff.append("}");
            elseNode.setBodyNode(bodyNode);
        }

        return node;
    }

    @Override
    public ASTNode visitWhile(ASTNode node) {
        WhileASTNode whileNode = (WhileASTNode) node;
        brNodeToJSON(whileNode);
        return node;
    }

    @Override
    public ASTNode visitArrAccess(ASTNode node) {
        ArrAccessASTNode arrAccessNode = (ArrAccessASTNode) node;
        knaryNodeToJSON(arrAccessNode);
        return node;
    }

    @Override
    public ASTNode visitArrLiteral(ASTNode node) {
        ArrLiteralASTNode arrLiteralNode = (ArrLiteralASTNode) node;
        arrLiteralNode.setTok(null);
        knaryNodeToJSON(arrLiteralNode);
        return node;
    }

    @Override
    public ASTNode visitFunDef(ASTNode node) {
        nodeToJSON(node);
        FunDefASTNode funDefNode = (FunDefASTNode) node;
        ParamListASTNode paramListNode = funDefNode.getParamListNode();
        ScopeASTNode bodyNode = funDefNode.getBodyNode();

        jsonStrBuff.append(",\"Param list\":");
        if (paramListNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            paramListNode = (ParamListASTNode) paramListNode.accept(this);
            jsonStrBuff.append("}");
            funDefNode.setParamListNode(paramListNode);
        }

        jsonStrBuff.append(",\"Body\":");
        if (bodyNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            bodyNode = (ScopeASTNode) bodyNode.accept(this);
            jsonStrBuff.append("}");
            funDefNode.setBodyNode(bodyNode);
        }

        return node;
    }

    @Override
    public ASTNode visitRet(ASTNode node) {
        RetASTNode retNode = (RetASTNode) node;
        unNodeToJSON(retNode);
        return node;
    }

    @Override
    public ASTNode visitBreak(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitCont(ASTNode node) {
        nodeToJSON(node);
        return node;
    }

    @Override
    public ASTNode visitScope(ASTNode node) {
        ScopeASTNode scopeNode = (ScopeASTNode) node;
        knaryNodeToJSON(scopeNode);
        return node;
    }

    @Override
    public ASTNode visitIfElse(ASTNode node) {
        IfElseASTNode ifElseNode = (IfElseASTNode) node;
        knaryNodeToJSON(ifElseNode);
        return node;
    }

    @Override
    public ASTNode visitIf(ASTNode node) {
        IfASTNode ifNode = (IfASTNode) node;
        brNodeToJSON(ifNode);
        return node;
    }

    @Override
    public ASTNode visitTypeAnn(ASTNode node) {
        nodeToJSON(node);
        TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) node;
        ASTNode left = typeAnnNode.getLeft();
        ASTNode dtypeNode = typeAnnNode.getDtypeNode();

        jsonStrBuff.append(",\"Left\":");
        if (left == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            left = left.accept(this);
            jsonStrBuff.append("}");
            typeAnnNode.setLeft(left);
        }

        jsonStrBuff.append(",\"Data type node\":");
        if (dtypeNode == null) {
            jsonStrBuff.append("\"null\"");
        } else {
            jsonStrBuff.append("{");
            dtypeNode = dtypeNode.accept(this);
            jsonStrBuff.append("}");
            typeAnnNode.setDtypeNode(dtypeNode);
        }

        return node;
    }

    @Override
    public ASTNode visitFunCall(ASTNode node) {
        FunCallASTNode funCallNode = (FunCallASTNode) node;
        knaryNodeToJSON(funCallNode);
        return node;
    }
}
