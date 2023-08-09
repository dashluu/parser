package parsers.control_transfer;

import ast.ASTNode;
import ast.BreakASTNode;
import exceptions.ErrMsg;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class BreakParser {
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
     * Parses a break statement.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a break statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseBreak(ParseContext context) throws IOException {
        // Check the break keyword
        ParseResult<Tok> kwResult = tokMatcher.parseTok(TokType.BREAK, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (!context.getScope().isInLoop()) {
            return context.raiseErr(new ErrMsg("Break statements can only exist inside a loop",
                    kwResult.getData()));
        }

        BreakASTNode breakNode = new BreakASTNode(kwResult.getData());
        return ParseResult.ok(breakNode);
    }
}
