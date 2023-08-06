package parsers.dtype;

import ast.ASTNode;
import ast.DtypeASTNode;
import ast.SimpleDtypeASTNode;
import exceptions.ErrMsg;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.TokParser;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class DtypeParser {
    private TokParser tokParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser a token parser.
     */
    public void init(TokParser tokParser) {
        this.tokParser = tokParser;
    }

    /**
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseTypeAnn(ParseContext context) throws IOException {
        // Try parsing ':'
        ParseResult<Tok> colonResult = tokParser.parseTok(TokType.COLON, context);
        if (colonResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (colonResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(colonResult.getFailTok());
        }

        // Parse a data type
        ParseResult<Tok> dtypeResult = tokParser.parseTok(TokType.ID, context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected a data type for type annotation",
                    dtypeResult.getFailTok()));
        }

        DtypeASTNode dtypeNode = new SimpleDtypeASTNode(dtypeResult.getData(), null);
        return ParseResult.ok(dtypeNode);
    }
}