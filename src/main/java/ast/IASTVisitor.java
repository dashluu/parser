package ast;

public interface IASTVisitor {
    ASTNode visitId(ASTNode node);

    ASTNode visitDtype(ASTNode node);

    ASTNode visitLiteral(ASTNode node);

    ASTNode visitVarDecl(ASTNode node);

    ASTNode visitVarDef(ASTNode node);

    ASTNode visitParamDecl(ASTNode node);

    ASTNode visitParamList(ASTNode node);

    ASTNode visitUnOp(ASTNode node);

    ASTNode visitBinOp(ASTNode node);

    ASTNode visitFunCall(ASTNode node);

    ASTNode visitFunDef(ASTNode node);

    ASTNode visitFunSign(ASTNode node);

    ASTNode visitRet(ASTNode node);

    ASTNode visitBreak(ASTNode node);

    ASTNode visitCont(ASTNode node);

    ASTNode visitScope(ASTNode node);

    ASTNode visitIfElse(ASTNode node);

    ASTNode visitIf(ASTNode node);

    ASTNode visitElse(ASTNode node);

    ASTNode visitWhile(ASTNode node);

    ASTNode visitArrAccess(ASTNode node);

    ASTNode visitArrLiteral(ASTNode node);

    ASTNode visitExprList(ASTNode node);
}
