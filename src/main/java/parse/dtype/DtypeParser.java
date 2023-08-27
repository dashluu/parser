package parse.dtype;

import ast.ASTNode;
import ast.ArrDtypeASTNode;
import ast.DtypeASTNode;
import ast.SimpleDtypeASTNode;
import exceptions.ErrMsg;
import lex.LexResult;
import lex.LexStatus;
import lex.Lexer;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import parse.utils.TokMatcher;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;
import types.ArrType;
import types.TypeInfo;

import java.io.IOException;

public class DtypeParser {
    private Lexer lexer;
    private TokMatcher tokMatcher;

    /**
     * Initializes the dependencies.
     *
     * @param lexer      a lexer.
     * @param tokMatcher a token matcher.
     */
    public void init(Lexer lexer, TokMatcher tokMatcher) {
        this.lexer = lexer;
        this.tokMatcher = tokMatcher;
    }

    /**
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseTypeAnn(ParseContext context) throws IOException {
        // Try parsing ':'
        ParseResult<Tok> colonResult = tokMatcher.match(TokType.COLON, context);
        if (colonResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (colonResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(colonResult.getFailTok());
        }

        // Parse a data type
        ParseResult<ASTNode> dtypeResult = parseDtype(context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected a data type for type annotation", dtypeResult.getFailTok()));
        }

        return ParseResult.ok(dtypeResult.getData());
    }

    /**
     * Parses a data type.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a data type.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseDtype(ParseContext context) throws IOException {
        ParseResult<ASTNode> arrDtypeResult = parseArrDtype(context);
        if (arrDtypeResult.getStatus() == ParseStatus.ERR || arrDtypeResult.getStatus() == ParseStatus.OK) {
            return arrDtypeResult;
        }
        return parseSimpleDtype(context);
    }

    /**
     * Parses a simple(non-array) data type.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a simple data type.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseSimpleDtype(ParseContext context) throws IOException {
        ParseResult<Tok> dtypeResult = tokMatcher.match(TokType.ID, context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(dtypeResult.getFailTok());
        }

        DtypeASTNode dtypeNode = new SimpleDtypeASTNode(dtypeResult.getData(), null);
        return ParseResult.ok(dtypeNode);
    }

    /**
     * Parses an array data type.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing an array data type.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseArrDtype(ParseContext context) throws IOException {
        // Look ahead for '['
        LexResult<Tok> lookaheadResult = lexer.lookahead(2, context);
        if (lookaheadResult.getStatus() == LexStatus.ERR) {
            return ParseResult.err();
        }

        Tok lookaheadTok = lookaheadResult.getData();
        if (lookaheadTok.getTokType() != TokType.LSQUARE) {
            return ParseResult.fail(lookaheadTok);
        }

        // Parse the array's core data type
        ParseResult<Tok> coreDtypeResult = tokMatcher.match(TokType.ID, context);
        if (coreDtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (coreDtypeResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected a data type for the array", coreDtypeResult.getFailTok()));
        }

        boolean end = false;
        ParseResult<Tok> squareResult;
        TypeInfo elmDtype = null;
        ArrType arrDtype;
        DtypeASTNode elmDtypeNode = new SimpleDtypeASTNode(coreDtypeResult.getData(), elmDtype);
        ArrDtypeASTNode arrDtypeNode = null;
        SrcRange srcRange = elmDtypeNode.getSrcRange();

        while (!end) {
            // Parse '['
            squareResult = tokMatcher.match(TokType.LSQUARE, context);
            if (squareResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (squareResult.getStatus() == ParseStatus.FAIL) {
                end = true;
            }

            if (!end) {
                // Parse ']'
                squareResult = tokMatcher.match(TokType.RSQUARE, context);
                if (squareResult.getStatus() == ParseStatus.ERR) {
                    return ParseResult.err();
                } else if (squareResult.getStatus() == ParseStatus.FAIL) {
                    return context.raiseErr(new ErrMsg("Missing ']'", squareResult.getFailTok()));
                }
            }

            srcRange = new SrcRange(srcRange.getStartPos(), squareResult.getData().getSrcRange().getEndPos());
            // The number of elements will be updated later by the semantic checker
            arrDtype = new ArrType(elmDtype);
            arrDtypeNode = new ArrDtypeASTNode(srcRange, arrDtype);
            arrDtypeNode.setElmDtypeNode(elmDtypeNode);
            elmDtype = arrDtype;
            elmDtypeNode = arrDtypeNode;
        }

        return ParseResult.ok(arrDtypeNode);
    }
}
