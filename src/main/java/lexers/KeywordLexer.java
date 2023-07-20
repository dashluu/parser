package lexers;

import toks.Tok;
import toks.TokType;
import utils.ParseContext;

import java.io.IOException;

public class KeywordLexer extends AlnumUnderscoreLexer {

    public KeywordLexer(LexReader reader) {
        super(reader);
    }

    /**
     * Reads a keyword token.
     *
     * @param context the parsing context.
     * @return a LexResult object as the result of reading a keyword token.
     * @throws IOException if the read operation causes an error.
     */
    public LexResult<Tok> read(ParseContext context) throws IOException {
        LexResult<Tok> result = super.read();
        if (result.getStatus() != LexStatus.OK) {
            return result;
        }
        Tok kwTok = result.getData();
        String kwStr = kwTok.getVal();
        TokType kwId = context.getKeywordTable().getId(kwStr);
        if (kwId == null) {
            // If the keyword is not found, put the token string back
            reader.putBack(kwStr);
            return LexResult.fail();
        }
        kwTok.setType(kwId);
        return LexResult.ok(kwTok);
    }
}
