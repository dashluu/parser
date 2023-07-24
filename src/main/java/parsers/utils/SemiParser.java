package parsers.utils;

import exceptions.ErrMsg;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class SemiParser {
    private TokParser tokParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser a parser that consumes valid tokens.
     */
    public void init(TokParser tokParser) {
        this.tokParser = tokParser;
    }

    /**
     * Parses a trailing semicolon.
     *
     * @param headResult the parsing result preceding the trailing semicolon.
     * @param context    the parsing context.
     * @param <E>        the type of data stored in ParseResult.
     * @return a ParseResult object as the result of parsing a trailing semicolon.
     * @throws IOException if there is an IO exception.
     */
    public <E> ParseResult<E> parseSemi(ParseResult<E> headResult, ParseContext context)
            throws IOException {
        ParseResult<Tok> semiResult = tokParser.parseTok(TokType.SEMICOLON, context);
        if (semiResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (semiResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing ';'", semiResult.getFailTok()));
        }

        return headResult;
    }
}
