package lex;

import exceptions.ErrMsg;
import parse.utils.ParseContext;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
    private final LexReader reader;
    private final AlnumUnderscoreLexer alnum_Lexer;
    private final NumLexer numLexer;
    private final KeywordLexer kwLexer;
    private final OpLexer opLexer;
    private final ArrayList<Tok> tokBuff = new ArrayList<>();

    public Lexer(LexReader reader) {
        this.reader = reader;
        alnum_Lexer = new AlnumUnderscoreLexer(reader);
        numLexer = new NumLexer(reader);
        kwLexer = new KeywordLexer(reader);
        opLexer = new OpLexer(reader);
    }

    /**
     * Consumes the next token by popping it off the buffer.
     *
     * @param context the parsing context.
     * @return a LexResult object as the result of consuming the next token.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> consume(ParseContext context) throws IOException {
        if (tokBuff.isEmpty()) {
            LexResult<Tok> tokResult = lookahead(context);
            if (tokResult.getStatus() == LexStatus.ERR) {
                return tokResult;
            }
        }

        return LexResult.ok(tokBuff.remove(0));
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
     * Performs multistep lookahead.
     *
     * @param steps   the number of steps to look ahead.
     * @param context the parsing context.
     * @return a LexResult object as the result of looking ahead the given number of steps.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> lookahead(int steps, ParseContext context) throws IOException {
        LexResult<Tok> tokResult;
        int buffSize = tokBuff.size();

        if (buffSize < steps) {
            for (int i = 0; i < steps - buffSize; ++i) {
                tokResult = extractTok(context);
                if (tokResult.getStatus() == LexStatus.ERR) {
                    return tokResult;
                }
                tokBuff.add(tokResult.getData());
            }
        }

        return LexResult.ok(tokBuff.get(steps - 1));
    }

    /**
     * Looks ahead to the next token without removing it from the buffer.
     *
     * @param context the parsing context.
     * @return a LexResult object as the result of peeking at the next token.
     * @throws IOException if the read operation causes an IO error.
     */
    public LexResult<Tok> lookahead(ParseContext context) throws IOException {
        // Read from the token buffer before extracting a token from the stream
        if (!tokBuff.isEmpty()) {
            return LexResult.ok(tokBuff.get(0));
        }

        LexResult<Tok> tokResult = extractTok(context);
        if (tokResult.getStatus() == LexStatus.ERR) {
            return tokResult;
        }

        tokBuff.add(tokResult.getData());
        return tokResult;
    }

    /**
     * Extracts a token from the stream.
     *
     * @param context the parsing context.
     * @return a LexResult object as the result of extracting a token.
     * @throws IOException if the read operation causes an IO error.
     */
    private LexResult<Tok> extractTok(ParseContext context) throws IOException {
        skipComment();
        reader.skipSpaces();

        // Check if the token is end-of-stream
        Tok tok;
        if (reader.peek() == LexReader.EOS) {
            SrcPos srcPos = reader.getSrcPos();
            SrcRange srcRange = new SrcRange(srcPos);
            tok = new Tok(null, TokType.EOS, srcRange);
            return LexResult.ok(tok);
        }

        // Check if the token is a keyword
        LexResult<Tok> result = kwLexer.read(context);
        if (result.getStatus() == LexStatus.OK) {
            return result;
        }

        // Check if the token is a number
        result = numLexer.read();
        if (result.getStatus() == LexStatus.OK) {
            return result;
        }

        // Check if the token is an operator
        result = opLexer.read(context);
        if (result.getStatus() == LexStatus.OK) {
            return result;
        }

        // Check if the token is id
        result = alnum_Lexer.read();
        if (result.getStatus() == LexStatus.OK) {
            tok = result.getData();
            tok.setTokType(TokType.ID);
            return result;
        }

        // Cannot read the next token
        return LexResult.err(new ErrMsg("Unable to get next token because of invalid syntax at '" +
                (char) reader.peek() + "'", reader.getSrcPos()));
    }
}
