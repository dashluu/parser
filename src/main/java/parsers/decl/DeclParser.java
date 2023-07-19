package parsers.decl;

import ast.*;
import exceptions.ErrMsg;
import parsers.expr.ExprParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class DeclParser {
    private TokParser tokParser;
    private ExprParser exprParser;
    private DeclSemanChecker semanChecker;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser  a token parser.
     * @param exprParser an expression parser.
     */
    public void init(TokParser tokParser, ExprParser exprParser, DeclSemanChecker semanChecker) {
        this.tokParser = tokParser;
        this.exprParser = exprParser;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a declaration statement.
     *
     * @param scope the scope surrounding the declaration statement.
     * @return a ParseResult object as the result of parsing the declaration statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseDecl(Scope scope) throws IOException {
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
            return ParseErr.raise(new ErrMsg("Expected an identifier", idResult.getFailTok()));
        }

        ASTNode idNode = idResult.getData();
        // Parse type annotation
        ParseResult<ASTNode> typeAnnResult = parseTypeAnn();
        ASTNode lhsNode;
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return typeAnnResult;
        } else if (typeAnnResult.getStatus() == ParseStatus.FAIL) {
            lhsNode = idNode;
        } else {
            TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) typeAnnResult.getData();
            typeAnnNode.setLeft(idNode);
            lhsNode = typeAnnNode;
        }

        // Parse assignment
        ParseResult<ASTNode> asgnmtResult = parseAsgnmt(mutable);
        if (asgnmtResult.getStatus() == ParseStatus.ERR) {
            return asgnmtResult;
        } else if (asgnmtResult.getStatus() == ParseStatus.FAIL) {
            if (typeAnnResult.getStatus() == ParseStatus.OK) {
                // No rhs expression but the data type is defined
                return semanChecker.checkSeman(lhsNode, scope);
            }
            String id = lhsNode.getTok().getVal();
            return ParseErr.raise(new ErrMsg("Cannot determine the data type of '" + id + "'",
                    asgnmtResult.getFailTok()));
        }

        // Parse rhs expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(scope);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Invalid declaration expression", exprResult.getFailTok()));
        }

        BinASTNode asgnmtNode = (BinASTNode) asgnmtResult.getData();
        asgnmtNode.setLeft(lhsNode);
        asgnmtNode.setRight(exprResult.getData());
        return semanChecker.checkSeman(asgnmtNode, scope);
    }

    /**
     * Parses a declaration head.
     *
     * @return a ParseResult object as the result of parsing a declaration head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Boolean> parseHead() throws IOException {
        ParseResult<Tok> headResult = tokParser.parseTok(TokType.VAR_DECL);
        if (headResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (headResult.getStatus() == ParseStatus.OK) {
            return ParseResult.ok(true);
        }

        // If variable keyword is not present, try parsing constant keyword
        headResult = tokParser.parseTok(TokType.CONST_DECL);
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
        ParseResult<Tok> result = tokParser.parseTok(TokType.ID);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok idTok = result.getData();
        ASTNode idNode = mutable ?
                new VarDeclASTNode(idTok, null) :
                new ConstDeclASTNode(idTok, null);
        return ParseResult.ok(idNode);
    }

    /**
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseTypeAnn() throws IOException {
        // Try parsing ':'
        ParseResult<Tok> typeAnnResult = tokParser.parseTok(TokType.COLON);
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (typeAnnResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(typeAnnResult.getFailTok());
        }

        // Parse a data type
        ParseResult<Tok> dtypeResult = tokParser.parseTok(TokType.ID);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Expected a data type for type annotation", dtypeResult.getFailTok()));
        }

        Tok typeAnnTok = typeAnnResult.getData();
        TypeAnnASTNode typeAnnNode = new TypeAnnASTNode(typeAnnTok, null);
        Tok dtypeTok = dtypeResult.getData();
        ASTNode dtypeNode = new ASTNode(dtypeTok, ASTNodeType.DTYPE, null);
        typeAnnNode.setDtypeNode(dtypeNode);
        return ParseResult.ok(typeAnnNode);
    }

    /**
     * Parses an assignment operator.
     *
     * @param mutable boolean value for declaration's mutability.
     * @return a ParseResult object as the result of parsing an assignment operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseAsgnmt(boolean mutable) throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.ASSIGNMENT);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        Tok asgnmtTok = result.getData();
        ASTNode asgnmtNode = mutable ?
                new VarDefASTNode(asgnmtTok, null) :
                new ConstDefASTNode(asgnmtTok, null);
        return ParseResult.ok(asgnmtNode);
    }
}
