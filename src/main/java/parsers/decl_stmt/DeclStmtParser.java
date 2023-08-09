package parsers.decl_stmt;

import ast.*;
import exceptions.ErrMsg;
import keywords.KeywordTable;
import parsers.dtype.DtypeParser;
import parsers.expr.ExprParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import parsers.utils.ParseContext;

import java.io.IOException;

public class DeclStmtParser {
    private TokMatcher tokMatcher;
    private DtypeParser dtypeParser;
    private ExprParser exprParser;
    private DeclStmtSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher   a token matcher.
     * @param dtypeParser  a data type parser.
     * @param exprParser   an expression parser.
     * @param semanChecker a semantic checker for the declaration statement.
     */
    public void init(TokMatcher tokMatcher, DtypeParser dtypeParser,
                     ExprParser exprParser, DeclStmtSemanChecker semanChecker) {
        this.tokMatcher = tokMatcher;
        this.dtypeParser = dtypeParser;
        this.exprParser = exprParser;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a variable declaration statement.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the variable declaration statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseDeclStmt(ParseContext context) throws IOException {
        this.context = context;
        // Parse head
        ParseResult<Tok> headResult = parseHead();
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(headResult.getFailTok());
        }

        Tok headTok = headResult.getData();
        boolean mutable = headTok.getVal().equals(KeywordTable.VAR);
        VarDeclASTNode declNode = new VarDeclASTNode(headTok, null);
        // Parse id
        ParseResult<ASTNode> idResult = parseId(mutable);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return idResult;
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected an identifier", idResult.getFailTok()));
        }

        IdASTNode idNode = (IdASTNode) idResult.getData();
        declNode.setIdNode(idNode);
        // Parse the data type with type annotation
        ParseResult<ASTNode> dtypeResult = dtypeParser.parseTypeAnn(context);
        DtypeASTNode dtypeNode;
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        } else if (dtypeResult.getStatus() == ParseStatus.OK) {
            dtypeNode = (DtypeASTNode) dtypeResult.getData();
            declNode.setDtypeNode(dtypeNode);
        }

        // Parse definition
        ParseResult<ASTNode> defResult = parseDef();
        if (defResult.getStatus() == ParseStatus.ERR) {
            return defResult;
        } else if (defResult.getStatus() == ParseStatus.FAIL) {
            if (dtypeResult.getStatus() == ParseStatus.OK) {
                // No rhs expression but the data type is defined
                return semanChecker.checkSeman(declNode, context);
            }
            return context.raiseErr(new ErrMsg("Cannot determine the data type of '" + idNode.getTok().getVal() + "'",
                    defResult.getFailTok()));
        }

        // Parse rhs expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid declaration expression", exprResult.getFailTok()));
        }

        VarDefASTNode defNode = (VarDefASTNode) defResult.getData();
        defNode.setVarDeclNode(declNode);
        defNode.setExprNode(exprResult.getData());
        return semanChecker.checkSeman(defNode, context);
    }

    /**
     * Parses a variable declaration head.
     *
     * @return a ParseResult object as the result of parsing a variable declaration head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Tok> parseHead() throws IOException {
        ParseResult<Tok> headResult = tokMatcher.parseTok(TokType.VAR_DECL, context);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.OK) {
            return headResult;
        }

        // If variable keyword is not present, try parsing constant keyword
        headResult = tokMatcher.parseTok(TokType.CONST_DECL, context);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        return headResult;
    }

    /**
     * Parses a variable declaration identifier.
     *
     * @param mutable true if the variable is mutable and false otherwise.
     * @return a ParseResult object as the result of parsing the variable declaration identifier.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseId(boolean mutable) throws IOException {
        ParseResult<Tok> result = tokMatcher.parseTok(TokType.ID, context);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok idTok = result.getData();
        IdASTNode idNode = new IdASTNode(idTok, null, mutable);
        return ParseResult.ok(idNode);
    }

    /**
     * Parses a variable definition.
     *
     * @return a ParseResult object as the result of parsing a variable definition.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseDef() throws IOException {
        ParseResult<Tok> result = tokMatcher.parseTok(TokType.ASSIGNMENT, context);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok defTok = result.getData();
        VarDefASTNode defNode = new VarDefASTNode(defTok, null);
        return ParseResult.ok(defNode);
    }
}
