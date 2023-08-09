package parsers.dtype;

import ast.ASTNode;
import ast.DtypeASTNode;
import ast.SimpleDtypeASTNode;
import exceptions.ErrMsg;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.TokMatcher;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class DtypeParser {
    private TokMatcher tokMatcher;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher a token matcher.
     */
    public void init(TokMatcher tokMatcher) {
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
        ParseResult<Tok> colonResult = tokMatcher.parseTok(TokType.COLON, context);
        if (colonResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (colonResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(colonResult.getFailTok());
        }

        // Parse a data type
        ParseResult<Tok> dtypeResult = tokMatcher.parseTok(TokType.ID, context);
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
