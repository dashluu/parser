package parsers.control_transfer;

import ast.ASTNode;
import ast.RetASTNode;
import exceptions.ErrMsg;
import parsers.expr.ExprParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;
import types.TypeTable;
import parsers.utils.ParseContext;

import java.io.IOException;

public class RetParser {
    private TokParser tokParser;
    private ExprParser exprParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser  a parser that consumes valid tokens.
     * @param exprParser an expression parser.
     */
    public void init(TokParser tokParser, ExprParser exprParser) {
        this.tokParser = tokParser;
        this.exprParser = exprParser;
    }

    /**
     * Parses a return statement using the given parsing information.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a return statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseRet(ParseContext context) throws IOException {
        // Check for the return keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.RET, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        // Detect unexpected return statement in a non-function scope
        TypeInfo retType = context.getScope().getRetDtype();
        if (retType == null) {
            return context.raiseErr(new ErrMsg("Return statements can only exist inside a function",
                    kwResult.getData()));
        }

        // Parse return expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL && !retType.equals(TypeTable.VOID)) {
            // When the status is "failed", that means an expression is missing
            return context.raiseErr(new ErrMsg("Invalid return expression", exprResult.getFailTok()));
        }

        ASTNode exprNode = null;
        TypeInfo exprDtype;

        if (exprResult.getStatus() == ParseStatus.FAIL) {
            // Missing expression indicates the return type is void
            exprDtype = TypeTable.VOID;
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

        retNode.setChild(exprNode);
        return ParseResult.ok(retNode);
    }
}