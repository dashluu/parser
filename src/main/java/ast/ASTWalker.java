package ast;

public class ASTWalker implements IASTVisitor {
    private record ASTNodeCallback(ASTWalker walker) implements IASTNodeCallback {
        @Override
        public ASTNode run(ASTNode node) {
            return node.accept(walker);
        }
    }

    private final ASTNodeCallback nodeCallback = new ASTNodeCallback(this);

    @Override
    public ASTNode visitVarId(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitConstId(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitParam(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitDtype(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitLiteral(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitVarDecl(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitVarDef(ASTNode node) {
        VarDefASTNode varDefNode = (VarDefASTNode) node;
        ASTNode right = varDefNode.getRight().accept(this);
        ASTNode left = varDefNode.getLeft().accept(this);
        varDefNode.setLeft(left);
        varDefNode.setRight(right);
        return node;
    }

    @Override
    public ASTNode visitConstDecl(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitConstDef(ASTNode node) {
        ConstDefASTNode constDefNode = (ConstDefASTNode) node;
        ASTNode right = constDefNode.getRight().accept(this);
        ASTNode left = constDefNode.getLeft().accept(this);
        constDefNode.setLeft(left);
        constDefNode.setRight(right);
        return node;
    }

    @Override
    public ASTNode visitParamDecl(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitParamList(ASTNode node) {
        ParamListASTNode paramListNode = (ParamListASTNode) node;
        paramListNode.callbackOnChildren(nodeCallback);
        return node;
    }

    @Override
    public ASTNode visitUnOp(ASTNode node) {
        UnOpASTNode unOpNode = (UnOpASTNode) node;
        ASTNode child = unOpNode.getChild().accept(this);
        unOpNode.setChild(child);
        return node;
    }

    @Override
    public ASTNode visitBinOp(ASTNode node) {
        BinOpASTNode binOpNode = (BinOpASTNode) node;
        ASTNode left = binOpNode.getLeft().accept(this);
        ASTNode right = binOpNode.getRight().accept(this);
        binOpNode.setLeft(left);
        binOpNode.setRight(right);
        return node;
    }

    @Override
    public ASTNode visitTypeAnn(ASTNode node) {
        TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) node;
        ASTNode left = typeAnnNode.getLeft().accept(this);
        typeAnnNode.setLeft(left);
        return node;
    }

    @Override
    public ASTNode visitFunCall(ASTNode node) {
        FunCallASTNode funCallNode = (FunCallASTNode) node;
        funCallNode.callbackOnChildren(nodeCallback);
        return node;
    }

    @Override
    public ASTNode visitFunDef(ASTNode node) {
        FunDefASTNode funDefNode = (FunDefASTNode) node;
        ParamListASTNode paramListNode = (ParamListASTNode) funDefNode.getParamListNode().accept(this);
        ScopeASTNode bodyNode = (ScopeASTNode) funDefNode.getBodyNode().accept(this);
        funDefNode.setParamListNode(paramListNode);
        funDefNode.setBodyNode(bodyNode);
        return node;
    }

    @Override
    public ASTNode visitRet(ASTNode node) {
        RetASTNode retNode = (RetASTNode) node;
        ASTNode child = retNode.getChild();
        if (child != null) {
            child = child.accept(this);
            retNode.setChild(child);
        }
        return node;
    }

    @Override
    public ASTNode visitBreak(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitCont(ASTNode node) {
        return node;
    }

    @Override
    public ASTNode visitScope(ASTNode node) {
        ScopeASTNode scopeNode = (ScopeASTNode) node;
        scopeNode.callbackOnChildren(nodeCallback);
        return node;
    }

    @Override
    public ASTNode visitIf(ASTNode node) {
        IfASTNode ifNode = (IfASTNode) node;
        ASTNode condNode = ifNode.getCondNode().accept(this);
        ScopeASTNode bodyNode = (ScopeASTNode) ifNode.getBodyNode().accept(this);
        ifNode.setCondNode(condNode);
        ifNode.setBodyNode(bodyNode);
        return node;
    }

    @Override
    public ASTNode visitElif(ASTNode node) {
        ElifASTNode elifNode = (ElifASTNode) node;
        ASTNode condNode = elifNode.getCondNode().accept(this);
        ScopeASTNode bodyNode = (ScopeASTNode) elifNode.getBodyNode().accept(this);
        elifNode.setCondNode(condNode);
        elifNode.setBodyNode(bodyNode);
        return node;
    }

    @Override
    public ASTNode visitElse(ASTNode node) {
        ElseASTNode elseNode = (ElseASTNode) node;
        ScopeASTNode bodyNode = (ScopeASTNode) elseNode.getBodyNode().accept(this);
        elseNode.setBodyNode(bodyNode);
        return node;
    }

    @Override
    public ASTNode visitWhile(ASTNode node) {
        WhileASTNode whileNode = (WhileASTNode) node;
        ASTNode condNode = whileNode.getCondNode().accept(this);
        ScopeASTNode bodyNode = (ScopeASTNode) whileNode.getBodyNode().accept(this);
        whileNode.setCondNode(condNode);
        whileNode.setBodyNode(bodyNode);
        return node;
    }
}
