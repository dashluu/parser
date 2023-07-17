package parsers.decl;

import exceptions.ErrMsg;
import parsers.expr.ExprSyntaxPass;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class DeclSyntaxPass {
    private TokParser tokParser;
    private SyntaxBuff syntaxBuff;
    private ExprSyntaxPass exprSyntaxPass;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser      a parser that consumes valid tokens.
     * @param exprSyntaxPass an object that checks the right-hand side(rhs) expression's syntax.
     */
    public void init(TokParser tokParser, ExprSyntaxPass exprSyntaxPass) {
        this.tokParser = tokParser;
        this.exprSyntaxPass = exprSyntaxPass;
    }

    /**
     * Attempts to consume and check the syntax of a declaration without the right-hand side(rhs) expression.
     *
     * @param syntaxBuff a buffer containing syntax information.
     * @return a ParseResult object as the result of checking the syntax of the declaration.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<SyntaxInfo> eatDecl(SyntaxBuff syntaxBuff) throws IOException {
        this.syntaxBuff = syntaxBuff;

        // Consume head
        SyntaxTag headTag;
        ParseResult<SyntaxInfo> headResult = eatVarHead();

        if (headResult.getStatus() == ParseStatus.ERR) {
            return headResult;
        } else if (headResult.getStatus() == ParseStatus.OK) {
            headTag = SyntaxTag.VAR_DECL;
        } else {
            // If variable keyword is not present, try consuming constant keyword
            headResult = eatConstHead();
            if (headResult.getStatus() == ParseStatus.ERR || headResult.getStatus() == ParseStatus.FAIL) {
                return headResult;
            }
            headTag = SyntaxTag.CONST_DECL;
        }

        // Consume id
        ParseResult<SyntaxInfo> idResult = eatId(headTag);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return idResult;
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Expected an id", idResult.getFailTok()));
        }

        // Consume data type
        ParseResult<SyntaxInfo> dtypeResult = eatTypeAnn();
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        }

        // Consume '='
        ParseResult<SyntaxInfo> asgnmtResult = eatAsgnmt();
        if (asgnmtResult.getStatus() == ParseStatus.ERR) {
            return asgnmtResult;
        } else if (asgnmtResult.getStatus() == ParseStatus.FAIL) {
            if (dtypeResult.getStatus() == ParseStatus.OK) {
                // No rhs expression but the data type is defined
                return dtypeResult;
            }
            String id = idResult.getData().getTok().getVal();
            return ParseErr.raise(new ErrMsg("Cannot determine the data type of '" + id + "'",
                    asgnmtResult.getFailTok()));
        }

        // Move the cursor to the back which is also the position of the assignment operator
        syntaxBuff.toBack();
        // Consume the rhs expression when '=' is present
        ParseResult<SyntaxInfo> exprResult = exprSyntaxPass.eatExpr(syntaxBuff);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Invalid declaration expression", exprResult.getFailTok()));
        }

        return exprResult;
    }

    /**
     * Attempts to consume a variable head.
     *
     * @return a ParseResult object as the result of consuming a variable head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatVarHead() throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.VAR_DECL);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        syntaxBuff.add(new SyntaxInfo(result.getData(), SyntaxTag.VAR_DECL));
        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume a constant head.
     *
     * @return a ParseResult object as the result of consuming a constant head.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatConstHead() throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.CONST_DECL);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        syntaxBuff.add(new SyntaxInfo(result.getData(), SyntaxTag.CONST_DECL));
        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume an id.
     *
     * @return a ParseResult object as the result of consuming an id.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatId(SyntaxTag headTag) throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.ID);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        SyntaxInfo syntaxInfo = new SyntaxInfo(result.getData(), headTag);
        syntaxBuff.add(syntaxInfo);
        // Return result contain the id's info to use later
        return ParseResult.ok(syntaxInfo);
    }

    /**
     * Attempts to consume and check the syntax of a type annotation.
     *
     * @return a ParseResult object as the result of checking the syntax of type annotation.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatTypeAnn() throws IOException {
        // Try consuming ':'
        ParseResult<Tok> result = tokParser.parseTok(TokType.COLON);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        // Consume data type
        // No need to add colon to the syntax buffer
        result = tokParser.parseTok(TokType.ID);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Expected a type id for type annotation", result.getFailTok()));
        }

        syntaxBuff.add(new SyntaxInfo(result.getData(), SyntaxTag.TYPE_ID));
        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume an assignment operator.
     *
     * @return a ParseResult object as the result of consuming an assignment operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatAsgnmt() throws IOException {
        ParseResult<Tok> result = tokParser.parseTok(TokType.ASSIGNMENT);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }
        syntaxBuff.add(new SyntaxInfo(result.getData(), SyntaxTag.ASGNMT));
        return ParseResult.ok(null);
    }
}
