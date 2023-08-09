package parsers.stmt;

import ast.ASTNode;
import parsers.control_transfer.BreakParser;
import parsers.control_transfer.ContParser;
import parsers.control_transfer.RetParser;
import parsers.decl_stmt.DeclStmtParser;
import parsers.expr.ExprParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class StmtParser {
    private TokMatcher tokMatcher;
    private SemiChecker semiChecker;
    private ExprParser exprParser;
    private DeclStmtParser declStmtParser;
    private RetParser retParser;
    private BreakParser breakParser;
    private ContParser contParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher     a token matcher.
     * @param semiChecker    a trailing semicolon checker.
     * @param exprParser     an expression parser.
     * @param declStmtParser a declaration statement parser.
     * @param retParser      a return statement parser.
     * @param breakParser    a break statement parser.
     * @param contParser     a continue statement parser.
     */
    public void init(TokMatcher tokMatcher, SemiChecker semiChecker, ExprParser exprParser, DeclStmtParser declStmtParser,
                     RetParser retParser, BreakParser breakParser, ContParser contParser) {
        this.tokMatcher = tokMatcher;
        this.semiChecker = semiChecker;
        this.exprParser = exprParser;
        this.declStmtParser = declStmtParser;
        this.retParser = retParser;
        this.breakParser = breakParser;
        this.contParser = contParser;
    }

    /**
     * Parses a statement and checks its semantics in a scope.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseStmt(ParseContext context) throws IOException {
        // Parse a declaration statement
        ParseResult<ASTNode> stmtResult = declStmtParser.parseDeclStmt(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return semiChecker.check(stmtResult, context);
        }

        // Parse a return statement
        stmtResult = retParser.parseRet(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return semiChecker.check(stmtResult, context);
        }

        // Parse a break statement
        stmtResult = breakParser.parseBreak(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return semiChecker.check(stmtResult, context);
        }

        // Parse a continue statement
        stmtResult = contParser.parseCont(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return semiChecker.check(stmtResult, context);
        }

        // Parse a expression
        stmtResult = exprParser.parseExpr(context);
        if (stmtResult.getStatus() == ParseStatus.ERR) {
            return stmtResult;
        } else if (stmtResult.getStatus() == ParseStatus.OK) {
            return semiChecker.check(stmtResult, context);
        }

        ParseResult<Tok> semiResult = tokMatcher.parseTok(TokType.SEMI, context);
        if (semiResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (semiResult.getStatus() == ParseStatus.FAIL) {
            return stmtResult;
        }

        // Empty statement in the form ';'
        return ParseResult.empty();
    }
}
