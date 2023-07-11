package parsers.stmt;

import ast.ASTNode;
import exceptions.ErrMsg;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import parsers.decl.DeclParser;
import parsers.expr.ExprParser;
import parsers.ret.RetParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class StmtParser {
    private Lexer lexer;
    private TokParser tokParser;
    private ExprParser exprParser;
    private DeclParser declParser;
    private RetParser retParser;
    private final ParseErr err = ParseErr.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param lexer      a lexer.
     * @param tokParser  a parser that consumes valid tokens.
     * @param exprParser an expression parser.
     * @param declParser a declaration statement parser.
     * @param retParser  a return statement parser.
     */
    public void init(Lexer lexer, TokParser tokParser, ExprParser exprParser,
                     DeclParser declParser, RetParser retParser) {
        this.lexer = lexer;
        this.tokParser = tokParser;
        this.exprParser = exprParser;
        this.declParser = declParser;
        this.retParser = retParser;
    }

    /**
     * Parses a statement and checks its semantics in a scope.
     *
     * @param scope the scope surrounding the statement.
     * @return a ParseResult object as the result of parsing a statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseStmt(Scope scope) throws IOException {
        // Parse a declaration statement
        ParseResult<ASTNode> result = declParser.parseDecl(scope);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        } else if (result.getStatus() == ParseStatus.OK) {
            if (parseSemicolon().getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            return result;
        }

        // Parse a return statement
        result = retParser.parseRet(scope);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        } else if (result.getStatus() == ParseStatus.OK) {
            if (parseSemicolon().getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            return result;
        }

        // Parse a expression
        result = exprParser.parseExpr(scope);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        } else if (result.getStatus() == ParseStatus.OK) {
            if (parseSemicolon().getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
        } else if (tokParser.parseTok(TokType.SEMICOLON).getStatus() == ParseStatus.OK) {
            // Empty statement of the form ';'
            return ParseResult.empty();
        }

        return result;
    }

    /**
     * Parses a semicolon that terminates a statement.
     *
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Tok> parseSemicolon() throws IOException {
        LexResult<Tok> tokResult = lexer.lookahead();
        if (tokResult.getStatus() == LexStatus.ERR) {
            return err.raise(tokResult.getErrMsg());
        }

        Tok tok = tokResult.getData();
        TokType tokType = tok.getType();
        int consumedRow = lexer.getConsumedRow();
        int lookaheadRow = tok.getRow();
        int col = tok.getCol();
        if (tokType != TokType.SEMICOLON || consumedRow != lookaheadRow) {
            return err.raise(new ErrMsg("Expected ';' on line " + consumedRow + " but got '" +
                    (tok.getType() == TokType.EOS ? "end of stream" : tok.getVal()) + "'",
                    lookaheadRow, col));
        }

        lexer.consume();
        return ParseResult.ok(tok);
    }
}
