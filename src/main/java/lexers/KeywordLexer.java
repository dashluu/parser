package lexers;

import keywords.KeywordTable;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class KeywordLexer extends AlnumUnderscoreLexer {
    private final KeywordTable kwTable = KeywordTable.getInst();

    public KeywordLexer(LexReader reader) {
        super(reader);
    }

    /**
     * Reads a keyword token.
     *
     * @return a LexResult object as the result of reading a keyword token.
     * @throws IOException if the read operation causes an error.
     */
    public LexResult<Tok> read() throws IOException {
        LexResult<Tok> result = super.read();
        if (result.getStatus() != LexStatus.OK) {
            return result;
        }
        Tok kwTok = result.getData();
        String kwStr = kwTok.getVal();
        TokType kwId = kwTable.getId(kwStr);
        if (kwId == null) {
            // If the keyword is not found, put the token string back
            reader.putBack(kwStr);
            return LexResult.fail();
        }
        kwTok.setType(kwId);
        return LexResult.ok(kwTok);
    }
}
