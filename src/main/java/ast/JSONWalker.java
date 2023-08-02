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
                .append(",\"Tok\":\"").append(tok).append("\"")
                .append(",\"Data type\":\"").append(dtype == null ? dtype : dtype.getId()).append("\"");
    }

    private void unNodeToJSON(UnASTNode unNode) {
        nodeToJSON(unNode);
        ASTNode child = unNode.getChild();
        jsonStrBuff.append(",\"Child\":");
        if (child == null) {
            jsonStrBuff.append(child);
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
            jsonStrBuff.append(left);
        } else {
            jsonStrBuff.append("{");
            left = left.accept(this);
            jsonStrBuff.append("}");
            binNode.setLeft(left);
        }

        jsonStrBuff.append(",\"Right\":");
        if (right == null) {
            jsonStrBuff.append(right);
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
            jsonStrBuff.append(condNode);
        } else {
            jsonStrBuff.append("{");
            condNode = condNode.accept(this);
            jsonStrBuff.append("}");
            brNode.setCondNode(condNode);
        }

        jsonStrBuff.append(",\"Body\":");
        if (bodyNode == null) {
            jsonStrBuff.append(bodyNode);
        } else {
            jsonStrBuff.append("{");
            bodyNode = (ScopeASTNode) bodyNode.accept(this);
            jsonStrBuff.append("}");
            brNode.setBodyNode(bodyNode);
        }
    }

    private void listNodeToJSON(ListASTNode listNode) {
        nodeToJSON(listNode);
        jsonStrBuff.append(",\"Source range\":\"").append(listNode.srcRange).append("\"");
        jsonStrBuff.append(",\"Children\":[");
        ListIterator<ASTNode> childrenIter = listNode.listIterator();
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
        listNodeToJSON(paramListNode);
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
            jsonStrBuff.append(bodyNode);
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
        nodeToJSON(node);
        ArrAccessASTNode arrAccessNode = (ArrAccessASTNode) node;
        IdASTNode idNode = arrAccessNode.getIdNode();
        ExprListASTNode indexListNode = arrAccessNode.getIndexListNode();

        jsonStrBuff.append(",\"Identifier\":");
        if (idNode == null) {
            jsonStrBuff.append(idNode);
        } else {
            jsonStrBuff.append("{");
            idNode = (IdASTNode) idNode.accept(this);
            jsonStrBuff.append("}");
            arrAccessNode.setIdNode(idNode);
        }

        jsonStrBuff.append(",\"Index list\":");
        if (indexListNode == null) {
            jsonStrBuff.append(indexListNode);
        } else {
            jsonStrBuff.append("{");
            indexListNode = (ExprListASTNode) indexListNode.accept(this);
            jsonStrBuff.append("}");
            arrAccessNode.setIndexListNode(indexListNode);
        }

        return node;
    }

    @Override
    public ASTNode visitArrLiteral(ASTNode node) {
        ArrLiteralASTNode arrLiteralNode = (ArrLiteralASTNode) node;
        listNodeToJSON(arrLiteralNode);
        return node;
    }

    @Override
    public ASTNode visitExprList(ASTNode node) {
        listNodeToJSON((ListASTNode) node);
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
            jsonStrBuff.append(paramListNode);
        } else {
            jsonStrBuff.append("{");
            paramListNode = (ParamListASTNode) paramListNode.accept(this);
            jsonStrBuff.append("}");
            funDefNode.setParamListNode(paramListNode);
        }

        jsonStrBuff.append(",\"Body\":");
        if (bodyNode == null) {
            jsonStrBuff.append(bodyNode);
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
        listNodeToJSON(scopeNode);
        return node;
    }

    @Override
    public ASTNode visitIfElse(ASTNode node) {
        IfElseASTNode ifElseNode = (IfElseASTNode) node;
        listNodeToJSON(ifElseNode);
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
            jsonStrBuff.append(left);
        } else {
            jsonStrBuff.append("{");
            left = left.accept(this);
            jsonStrBuff.append("}");
            typeAnnNode.setLeft(left);
        }

        jsonStrBuff.append(",\"Data type node\":");
        if (dtypeNode == null) {
            jsonStrBuff.append(dtypeNode);
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
        nodeToJSON(node);
        FunCallASTNode funCallNode = (FunCallASTNode) node;
        IdASTNode idNode = funCallNode.getIdNode();
        ExprListASTNode argListNode = funCallNode.getArgListNode();

        jsonStrBuff.append(",\"Identifier\":");
        if (idNode == null) {
            jsonStrBuff.append(idNode);
        } else {
            jsonStrBuff.append("{");
            idNode = (IdASTNode) idNode.accept(this);
            jsonStrBuff.append("}");
            funCallNode.setIdNode(idNode);
        }

        jsonStrBuff.append(",\"Argument list\":");
        if (argListNode == null) {
            jsonStrBuff.append(argListNode);
        } else {
            jsonStrBuff.append("{");
            argListNode = (ExprListASTNode) argListNode.accept(this);
            jsonStrBuff.append("}");
            funCallNode.setArgListNode(argListNode);
        }

        return node;
    }
}
