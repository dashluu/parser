package ast;

public interface IASTVisitor {
    void visitNode(ASTNode node);

    void visitLiteral(LiteralASTNode literalNode);

    void visitVarDecl(VarDeclASTNode varDeclNode);

    void visitVarDef(VarDefASTNode varDefNode);

    void visitConstDecl(ConstDeclASTNode constDeclNode);

    void visitConstDef(ConstDefASTNode constDefNode);

    void visitParamDecl(ParamDeclASTNode paramDeclNode);

    void visitParamList(ParamListASTNode paramListNode);

    void visitUnOp(UnOpASTNode unOpASTNode);

    void visitBinOp(BinOpASTNode binOpNode);

    void visitTypeAnn(TypeAnnASTNode typeAnnNode);

    void visitFunCall(FunCallASTNode funCallNode);

    void visitFunDef(FunDefASTNode funDefNode);

    void visitRet(RetASTNode retNode);

    void visitScope(ScopeASTNode scopeNode);

    void visitIf(IfASTNode ifNode);

    void visitElif(ElifASTNode elifNode);

    void visitElse(ElseASTNode elseNode);

    void visitWhile(WhileASTNode whileNode);
}
