package parse.utils;

import exceptions.ErrMsg;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class SemiChecker {
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
     * Checks for a trailing semicolon.
     *
     * @param headResult the parsing result preceding the trailing semicolon.
     * @param context    the parsing context.
     * @param <E>        the type of data stored in ParseResult.
     * @return a ParseResult object as the result of checking a trailing semicolon.
     * @throws IOException if there is an IO exception.
     */
    public <E> ParseResult<E> check(ParseResult<E> headResult, ParseContext context)
            throws IOException {
        ParseResult<Tok> semiResult = tokMatcher.match(TokType.SEMI, context);
        if (semiResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (semiResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing ';'", semiResult.getFailTok()));
        }

        return headResult;
    }
}
