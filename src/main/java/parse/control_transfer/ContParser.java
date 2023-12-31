package parse.control_transfer;

import ast.ASTNode;
import ast.ContASTNode;
import exceptions.ErrMsg;
import parse.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ContParser {
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
     * Parses a continue statement.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a continue statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseCont(ParseContext context) throws IOException {
        // Check the continue keyword
        ParseResult<Tok> kwResult = tokMatcher.parseTok(TokType.CONT, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (!context.getScope().isInLoop()) {
            return context.raiseErr(new ErrMsg("Continue statements can only exist inside a loop",
                    kwResult.getData()));
        }

        ContASTNode contNode = new ContASTNode(kwResult.getData());
        return ParseResult.ok(contNode);
    }
}
