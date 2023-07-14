package ast;

public interface IASTVisitor {
    void visitLiteral(LiteralASTNode literalNode);

    void visitVarId(VarIdASTNode varIdNode);

    void visitVarDecl(VarDeclASTNode varDeclNode);

    void visitVarDef(VarDefASTNode varDefNode);

    void visitConstId(ConstIdASTNode constIdNode);

    void visitConstDecl(ConstDeclASTNode constDeclNode);

    void visitConstDef(ConstDefASTNode constDefNode);

    void visitParam(ParamASTNode paramNode);

    void visitParamDecl(ParamDeclASTNode paramDeclNode);

    void visitParamList(ParamListASTNode paramListNode);

    void visitUnOp(UnOpASTNode unOpASTNode);

    void visitBinOp(BinOpASTNode binOpNode);

    void visitDtype(DtypeASTNode dtypeNode);

    void visitFunCall(FunCallASTNode funCallNode);

    void visitFunDef(FunDefASTNode funDefNode);

    void visitRet(RetASTNode retNode);

    void visitScope(ScopeASTNode scopeNode);

    void visitIf(IfASTNode ifNode);

    void visitElif(ElifASTNode elifNode);

    void visitElse(ElseASTNode elseNode);
}
