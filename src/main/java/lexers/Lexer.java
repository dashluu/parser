package lexers;

import exceptions.ErrMsg;
import parsers.utils.ParseContext;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;
import java.util.ArrayDeque;

public class Lexer {
    private final LexReader reader;
    private final AlnumUnderscoreLexer alnum_Lexer;
    private final NumLexer numLexer;
    private final KeywordLexer kwLexer;
    private final OpLexer opLexer;
    private final ArrayDeque<Tok> tokBuff = new ArrayDeque<>();

    public Lexer(LexReader reader) {
        this.reader = reader;
        alnum_Lexer = new AlnumUnderscoreLexer(reader);
        numLexer = new NumLexer(reader);
        kwLexer = new KeywordLexer(reader);
        opLexer = new OpLexer(reader);
    }

    /**
     * Pops the next token off the buffer.
     */
    public void consume() {
        tokBuff.removeFirst();
    }

    /**
     * Skips the leading whitespaces and a trailing comment if there is any.
     *
     * @throws IOException if the read operation causes an IO error.
     */
    private void skipComment() throws IOException {
        reader.skipSpaces();
        reader.skipSinglelineComment();
        reader.skipMultilineComment();
    }

    /**
     * Looks ahead to the next token without removing it from the stream.
     *
     * @param context the parsing context.
     * @return a LexResult object as the result of peeking at the next token.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> lookahead(ParseContext context) throws IOException {
        // Reads from the token buffer before extracting characters from the stream
        if (!tokBuff.isEmpty()) {
            return LexResult.ok(tokBuff.peekFirst());
        }
        skipComment();
        reader.skipSpaces();
        // Check if the token is EOF
        Tok tok;
        if (reader.peek() == LexReader.EOS) {
            SrcPos srcPos = reader.getSrcPos();
            SrcRange srcRange = new SrcRange(srcPos);
            tok = new Tok(null, TokType.EOS, srcRange);
            tokBuff.addLast(tok);
            return LexResult.ok(tok);
        }
        // Check if the token is a keyword
        LexResult<Tok> result = kwLexer.read(context);
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is a number
        result = numLexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is an operator
        result = opLexer.read(context);
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tokBuff.addLast(tok);
            return result;
        }
        // Check if the token is id
        result = alnum_Lexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tok.setType(TokType.ID);
            tokBuff.addLast(tok);
            return result;
        }
        // Cannot read the next token
        return LexResult.err(new ErrMsg("Unable to get next token because of invalid syntax at '" +
                (char) reader.peek() + "'", reader.getSrcPos()));
    }
}
