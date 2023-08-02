package parsers.decl;

import ast.*;
import exceptions.ErrMsg;
import keywords.KeywordTable;
import parsers.expr.ExprParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import parsers.utils.ParseContext;

import java.io.IOException;

public class DeclParser {
    private TokParser tokParser;
    private TypeAnnParser typeAnnParser;
    private ExprParser exprParser;
    private DeclSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser     a token parser.
     * @param typeAnnParser a type annotation parser.
     * @param exprParser    an expression parser.
     * @param semanChecker  a declaration semantics checker.
     */
    public void init(TokParser tokParser, TypeAnnParser typeAnnParser,
                     ExprParser exprParser, DeclSemanChecker semanChecker) {
        this.tokParser = tokParser;
        this.typeAnnParser = typeAnnParser;
        this.exprParser = exprParser;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a variable declaration or definition.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the variable declaration or definition.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseVarDecl(ParseContext context) throws IOException {
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
        VarDeclASTNode varDeclNode = new VarDeclASTNode(headTok, null);
        // Parse id
        ParseResult<ASTNode> idResult = parseId(mutable);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return idResult;
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected an identifier", idResult.getFailTok()));
        }

        IdASTNode idNode = (IdASTNode) idResult.getData();
        varDeclNode.setIdNode(idNode);
        // Parse the data type with type annotation
        ParseResult<ASTNode> typeAnnResult = typeAnnParser.parseTypeAnn(context);
        DtypeASTNode dtypeNode;
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return typeAnnResult;
        } else if (typeAnnResult.getStatus() == ParseStatus.OK) {
            dtypeNode = (DtypeASTNode) typeAnnResult.getData();
            varDeclNode.setDtypeNode(dtypeNode);
        }

        // Parse assignment
        ParseResult<ASTNode> asgnmtResult = parseAsgnmt();
        if (asgnmtResult.getStatus() == ParseStatus.ERR) {
            return asgnmtResult;
        } else if (asgnmtResult.getStatus() == ParseStatus.FAIL) {
            if (typeAnnResult.getStatus() == ParseStatus.OK) {
                // No rhs expression but the data type is defined
                return semanChecker.checkSeman(varDeclNode, context);
            }
            return context.raiseErr(new ErrMsg("Cannot determine the data type of '" + idNode.getTok().getVal() + "'",
                    asgnmtResult.getFailTok()));
        }

        // Parse rhs expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid declaration expression", exprResult.getFailTok()));
        }

        BinASTNode asgnmtNode = (BinASTNode) asgnmtResult.getData();
        asgnmtNode.setLeft(varDeclNode);
        asgnmtNode.setRight(exprResult.getData());
        return semanChecker.checkSeman(asgnmtNode, context);
    }

    /**
     * Parses a variable declaration head.
     *
     * @return a ParseResult object as the result of parsing a variable declaration head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Tok> parseHead() throws IOException {
        ParseResult<Tok> headResult = tokParser.parseTok(TokType.VAR_DECL, context);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.OK) {
            return headResult;
        }

        // If variable keyword is not present, try parsing constant keyword
        headResult = tokParser.parseTok(TokType.CONST_DECL, context);
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
        ParseResult<Tok> result = tokParser.parseTok(TokType.ID, context);
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
     * Parses an assignment operator.
     *
     * @return a ParseResult object as the result of parsing an assignment operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseAsgnmt() throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.ASSIGNMENT, context);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok asgnmtTok = result.getData();
        VarDefASTNode asgnmtNode = new VarDefASTNode(asgnmtTok, null);
        return ParseResult.ok(asgnmtNode);
    }
}
