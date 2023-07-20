package parsers.parse_utils;

import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class TokParser {
    private Lexer lexer;

    /**
     * Initializes the dependencies.
     *
     * @param lexer a lexer.
     */
    public void init(Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Parses a token.
     *
     * @param tokType the expected token type.
     * @return a ParseResult as the result of parsing a token.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<Tok> parseTok(TokType tokType) throws IOException {
        LexResult<Tok> tokResult = lexer.lookahead();
        if (tokResult.getStatus() != LexStatus.OK) {
            return ParseErr.raise(tokResult.getErrMsg());
        }
        Tok tok = tokResult.getData();
        if (tok.getType() != tokType) {
            return ParseResult.fail(tok);
        }
        lexer.consume();
        return ParseResult.ok(tok);
    }
}
