package lex;

import exceptions.ErrMsg;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class AlnumUnderscoreLexer {
    protected final LexReader reader;

    public AlnumUnderscoreLexer(LexReader reader) {
        this.reader = reader;
    }

    /**
     * Reads a token alphanumeric and underscore characters.
     * Grammar: ('_'|('a'-'z')|('A'-'Z'))('_'|('a'-'z')|('A'-'Z')|('0'-'9'))*
     *
     * @return a LexResult object as the result of reading an alphanumeric token.
     * @throws IOException if the read operation causes an error.
     */
    public LexResult<Tok> read() throws IOException {
        int c;

        // Check if the first character is end-of-stream or neither a letter nor '_'
        if ((c = reader.peek()) == LexReader.EOS || (!Character.isAlphabetic(c) && c != '_')) {
            return LexResult.fail();
        }

        StringBuilder tokStr = new StringBuilder();
        boolean end = false;
        SrcPos startPos = reader.getSrcPos();

        // Consume the character from the stream until it is a separator or a valid special character
        while (!reader.isSep(c) && !end) {
            if (reader.isAlnumUnderscore(c)) {
                tokStr.append((char) c);
                reader.read();
                c = reader.peek();
            } else if (reader.isSpecialChar(c)) {
                end = true;
            } else {
                return LexResult.err(new ErrMsg("Invalid character '" + c + "' after '" + tokStr + "'",
                        reader.getSrcPos()));
            }
        }

        // The token string cannot be empty
        SrcPos endPos = reader.getSrcPos();
        SrcRange srcRange = new SrcRange(startPos, endPos);
        Tok tok = new Tok(tokStr.toString(), TokType.UNKNOWN, srcRange);
        return LexResult.ok(tok);
    }
}
