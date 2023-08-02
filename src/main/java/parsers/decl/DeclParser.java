package parsers.decl;

import ast.*;
import exceptions.ErrMsg;
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
     * Parses a declaration statement.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the declaration statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseDecl(ParseContext context) throws IOException {
        this.context = context;
        // Parse head
        ParseResult<Boolean> headResult = parseHead();
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(headResult.getFailTok());
        }

        boolean mutable = headResult.getData();
        // Parse id
        ParseResult<ASTNode> idResult = parseId(mutable);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return idResult;
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected an identifier", idResult.getFailTok()));
        }

        ASTNode idNode = idResult.getData();
        // Parse type annotation
        ParseResult<ASTNode> typeAnnResult = typeAnnParser.parseTypeAnn(context);
        ASTNode lhsNode;
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return typeAnnResult;
        } else if (typeAnnResult.getStatus() == ParseStatus.FAIL) {
            lhsNode = idNode;
        } else {
            TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) typeAnnResult.getData();
            typeAnnNode.setLeft(idNode);
            typeAnnNode.updateSrcRange();
            lhsNode = typeAnnNode;
        }

        // Parse assignment
        ParseResult<ASTNode> asgnmtResult = parseAsgnmt(mutable);
        if (asgnmtResult.getStatus() == ParseStatus.ERR) {
            return asgnmtResult;
        } else if (asgnmtResult.getStatus() == ParseStatus.FAIL) {
            if (typeAnnResult.getStatus() == ParseStatus.OK) {
                // No rhs expression but the data type is defined
                return semanChecker.checkSeman(lhsNode, context);
            }
            String id = lhsNode.getTok().getVal();
            return context.raiseErr(new ErrMsg("Cannot determine the data type of '" + id + "'",
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
        asgnmtNode.setLeft(lhsNode);
        asgnmtNode.setRight(exprResult.getData());
        asgnmtNode.updateSrcRange();
        return semanChecker.checkSeman(asgnmtNode, context);
    }

    /**
     * Parses a declaration head.
     *
     * @return a ParseResult object as the result of parsing a declaration head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Boolean> parseHead() throws IOException {
        ParseResult<Tok> headResult = tokParser.parseTok(TokType.VAR_DECL, context);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.OK) {
            return ParseResult.ok(true);
        }

        // If variable keyword is not present, try parsing constant keyword
        headResult = tokParser.parseTok(TokType.CONST_DECL, context);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(headResult.getFailTok());
        }

        return ParseResult.ok(false);
    }

    /**
     * Parses a declaration identifier.
     *
     * @param mutable boolean value for declaration's mutability.
     * @return a ParseResult object as the result of parsing the declaration identifier.
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
        ASTNode idNode = new VarDeclASTNode(idTok, null, mutable);
        return ParseResult.ok(idNode);
    }

    /**
     * Parses an assignment operator.
     *
     * @param mutable boolean value for declaration's mutability.
     * @return a ParseResult object as the result of parsing an assignment operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseAsgnmt(boolean mutable) throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.ASSIGNMENT, context);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok asgnmtTok = result.getData();
        ASTNode asgnmtNode = new VarDefASTNode(asgnmtTok, null, mutable);
        return ParseResult.ok(asgnmtNode);
    }
}
