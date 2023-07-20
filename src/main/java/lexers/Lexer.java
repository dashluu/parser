package lexers;

import exceptions.ErrMsg;
import toks.Tok;
import toks.TokType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;

public class Lexer {
    private final LexReader reader;
    private final AlnumUnderscoreLexer alnum_Lexer;
    private final NumLexer numLexer;
    private final KeywordLexer kwLexer;
    private final OpLexer opLexer;
    private final ArrayDeque<Tok> tokBuff = new ArrayDeque<>();

    // Row number of a token being peeked
    private int row = 1;
    // Column number
    private int col = 0;

    public Lexer(Reader reader) {
        this.reader = new LexReader(reader);
        alnum_Lexer = new AlnumUnderscoreLexer(this.reader);
        numLexer = new NumLexer(this.reader);
        kwLexer = new KeywordLexer(this.reader);
        opLexer = new OpLexer(this.reader);
    }

    /**
     * Gets the row number in lexer.
     *
     * @return an integer as the row number.
     */
    public int getRow() {
        return row;
    }

    /**
     * Looks ahead to and pops the next token from the buffer.
     *
     * @return a LexResult object as the result of consuming a token.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> consume() throws IOException {
        LexResult<Tok> result = lookahead();
        if (result.getStatus() != LexStatus.OK) {
            return result;
        }
        Tok tok = tokBuff.removeFirst();
        return LexResult.ok(tok);
    }

    /**
     * Updates the row and column number in lexer.
     *
     * @param tok the token whose row and column is to be updated.
     */
    private void updateRowCol(Tok tok) {
        if (row != tok.getRow()) {
            row = tok.getRow();
            col = 1;
        } else {
            ++col;
        }
        tok.setCol(col);
    }

    /**
     * Looks ahead to the next token without removing it from the stream.
     *
     * @return a LexResult object as the result of peeking at the next token.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> lookahead() throws IOException {
        // Reads from the token buffer before extracting characters from the stream
        if (!tokBuff.isEmpty()) {
            return LexResult.ok(tokBuff.peekFirst());
        }
        // Skip the white spaces
        reader.skipSpaces();
        // Check if the token is EOF
        Tok tok;
        if (reader.peek() == LexReader.EOS) {
            tok = new Tok(null, TokType.EOS, row, -1);
            updateRowCol(tok);
            tokBuff.addLast(tok);
            return LexResult.ok(tok);
        }
        // Check if the token is a keyword
        LexResult<Tok> result = kwLexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            updateRowCol(tok);
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is a number
        result = numLexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            updateRowCol(tok);
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is an operator
        result = opLexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            updateRowCol(tok);
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is id
        result = alnum_Lexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tok.setType(TokType.ID);
            updateRowCol(tok);
            tokBuff.addLast(tok);
            return result;
        }
        // Cannot read the next token
        return LexResult.err(new ErrMsg("Unable to get next token because of invalid syntax at '" +
                (char) reader.peek() + "'", reader.getRow(), col));
    }
}
