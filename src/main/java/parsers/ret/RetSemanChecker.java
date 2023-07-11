package parsers.ret;

import ast.ASTNode;
import ast.UnASTNode;
import exceptions.ErrMsg;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.Scope;
import types.TypeInfo;

public class RetSemanChecker {
    /**
     * Checks the semantics of the return statement.
     *
     * @param retNode the return statement AST's root.
     * @param scope   the scope surrounding the return statement.
     * @return a ParseResult object as the result of type checking the return statement's semantics.
     */
    public ParseResult<ASTNode> checkSeman(UnASTNode retNode, Scope scope) {
        TypeInfo retType = scope.getRetType();
        if (!retNode.getDtype().equals(retType)) {
            ASTNode exprNode = retNode.getChild();
            return ParseErr.getInst().raise(new ErrMsg("Return type is not '" + retType.id() + "'",
                    (exprNode != null ? exprNode.getTok() : retNode.getTok())));
        }
        return ParseResult.ok(retNode);
    }
}
