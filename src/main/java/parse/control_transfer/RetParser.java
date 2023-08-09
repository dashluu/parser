package parse.control_transfer;

import ast.ASTNode;
import ast.RetASTNode;
import exceptions.ErrMsg;
import parse.expr.ExprParser;
import parse.scope.RetState;
import parse.scope.Scope;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import parse.utils.TokMatcher;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;
import types.VoidType;

import java.io.IOException;

public class RetParser {
    private TokMatcher tokMatcher;
    private ExprParser exprParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher a token matcher.
     * @param exprParser an expression parser.
     */
    public void init(TokMatcher tokMatcher, ExprParser exprParser) {
        this.tokMatcher = tokMatcher;
        this.exprParser = exprParser;
    }

    /**
     * Parses a return statement.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a return statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseRet(ParseContext context) throws IOException {
        // Check for the return keyword
        ParseResult<Tok> kwResult = tokMatcher.parseTok(TokType.RET, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        // Detect unexpected return statement in a non-function scope
        TypeInfo retType = context.getScope().isInFun();
        if (retType == null) {
            return context.raiseErr(new ErrMsg("Return statements can only exist inside a function",
                    kwResult.getData()));
        }

        // Parse return expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL && !retType.equals(VoidType.getInst())) {
            // When the status is "failed", that means an expression is missing
            return context.raiseErr(new ErrMsg("Invalid return expression", exprResult.getFailTok()));
        }

        ASTNode exprNode = null;
        TypeInfo exprDtype;

        if (exprResult.getStatus() == ParseStatus.FAIL) {
            // Missing expression indicates the return type is void
            exprDtype = VoidType.getInst();
        } else {
            exprNode = exprResult.getData();
            exprDtype = exprNode.getDtype();
        }

        // Check if the return type is as expected
        Tok kwTok = kwResult.getData();
        RetASTNode retNode = new RetASTNode(kwTok, exprDtype);
        if (!retNode.getDtype().equals(retType)) {
            return context.raiseErr(new ErrMsg("Return type is not '" + retType.getId() + "'", kwTok));
        }

        if (exprNode != null) {
            retNode.setExprNode(exprNode);
        }

        // Update the return state of the current scope
        Scope scope = context.getScope();
        scope.setRetState(RetState.EXIST);
        return ParseResult.ok(retNode);
    }
}
