package ast;

public interface IASTVisitor {
    void visitLiteral(LiteralASTNode literalNode);

    void visitVarId(VarIdASTNode varIdNode);

    void visitVarDecl(VarDeclASTNode varDeclNode);

    void visitConstId(ConstIdASTNode constIdNode);

    void visitConstDecl(ConstDeclASTNode constDeclNode);

    void visitParam(ParamASTNode paramNode);

    void visitParamDecl(ParamDeclASTNode paramDeclNode);

    void visitParamList(ParamListASTNode paramListNode);

    void visitUnOp(UnOpASTNode unOpASTNode);

    void visitBinOp(BinOpASTNode binOpNode);

    void visitDef(DefASTNode defNode);

    void visitTypeConv(TypeConvASTNode typeConvNode);

    void visitFunCall(FunCallASTNode funCallNode);

    void visitFunDef(FunDefASTNode funDefNode);

    void visitRet(RetASTNode retNode);

    void visitScope(ScopeASTNode scopeNode);
}
