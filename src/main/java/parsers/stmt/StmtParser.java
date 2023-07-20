package parsers.stmt;

import ast.ASTNode;
import exceptions.ErrMsg;
import parsers.decl.DeclParser;
import parsers.expr.ExprParser;
import parsers.ret.RetParser;
import parsers.parse_utils.*;
import toks.Tok;
import toks.TokType;
import utils.ParseContext;

import java.io.IOException;

public class StmtParser {
    private TokParser tokParser;
    private ExprParser exprParser;
    private DeclParser declParser;
    private RetParser retParser;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser  a parser that consumes valid tokens.
     * @param exprParser an expression parser.
     * @param declParser a declaration statement parser.
     * @param retParser  a return statement parser.
     */
    public void init(TokParser tokParser, ExprParser exprParser, DeclParser declParser, RetParser retParser) {
        this.tokParser = tokParser;
        this.exprParser = exprParser;
        this.declParser = declParser;
        this.retParser = retParser;
    }

    /**
     * Parses a statement and checks its semantics in a scope.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseStmt(ParseContext context) throws IOException {
        this.context = context;
        // Parse a declaration statement
        ParseResult<ASTNode> stmtResult = declParser.parseDecl(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return parseSemicolon(stmtResult);
        }

        // Parse a return statement if failed
        stmtResult = retParser.parseRet(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return parseSemicolon(stmtResult);
        }

        // Parse a expression
        stmtResult = exprParser.parseExpr(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return parseSemicolon(stmtResult);
        }

        ParseResult<Tok> sepResult = tokParser.parseTok(TokType.SEMICOLON, context);
        if (sepResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (sepResult.getStatus() == ParseStatus.FAIL) {
            return stmtResult;
        }

        // Empty statement in the form ';'
        return ParseResult.empty();
    }

    /**
     * Parses a semicolon as the terminator of a statement.
     *
     * @param stmtResult the result of parsing a statement.
     * @return a ParseResult object as the result of parsing a semicolon following a statement.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseSemicolon(ParseResult<ASTNode> stmtResult) throws IOException {
        ParseResult<Tok> sepResult = tokParser.parseTok(TokType.SEMICOLON, context);
        if (sepResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (sepResult.getStatus() == ParseStatus.FAIL) {
            // An empty expression is valid without a trailing ';'
            return ParseErr.raise(new ErrMsg("Missing ';'", sepResult.getFailTok()));
        }

        return stmtResult;
    }
}
