package lexers;

import operators.OpTable;
import toks.Tok;
import toks.TokType;
import utils.ParseContext;

import java.io.IOException;

public class OpLexer {
    private final LexReader reader;

    public OpLexer(LexReader reader) {
        this.reader = reader;
    }

    /**
     * Reads an operator token.
     *
     * @param context the parsing context.
     * @return LexResult object as the result of reading an operator token.
     * @throws IOException if there is an IO exception.
     */
    public LexResult<Tok> read(ParseContext context) throws IOException {
        int c;
        String tokVal = "";
        StringBuilder tmpStr = new StringBuilder();
        TokType tmpTokType, opId = null;
        OpTable opTable = context.getOpTable();
        boolean end = false;

        while ((c = reader.peek()) != LexReader.EOS && !reader.isSpace(c) && !end) {
            tmpStr.append((char) c);
            // Consume the character now, but we'll put it back later
            reader.read();
            // Check if the string is a prefix of any operator in the table
            end = !opTable.isOpPrefixStr(tmpStr.toString());
            if (!end) {
                // Check if the string matches any operator in the table
                tmpTokType = opTable.getId(tmpStr.toString());
                if (tmpTokType != null) {
                    tokVal = tmpStr.toString();
                    opId = tmpTokType;
                }
            }
        }

        if (tmpStr.length() > tokVal.length()) {
            // Put back everything that has been read pass the recognized token
            String suffix = tmpStr.substring(tokVal.length());
            reader.putBack(suffix);
        }

        if (tokVal.isEmpty()) {
            return LexResult.fail();
        }

        Tok opTok = new Tok(tokVal, opId, reader.getRow());
        return LexResult.ok(opTok);
    }
}
