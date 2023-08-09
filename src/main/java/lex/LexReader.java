package lex;

import toks.SrcPos;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class LexReader {
    private final ArrayDeque<Integer> buff = new ArrayDeque<>();
    private final Reader reader;
    public final static int EOS = -1;
    private final static String SPECIAL_CHARS = "(){}[]+-*/%~!&|<>=,.;:_";
    // List of end-of-line columns
    private final List<Integer> lnEndList = new ArrayList<>();

    public LexReader(Reader reader) {
        this.reader = reader;
        lnEndList.add(1);
    }

    /**
     * Determines if the character is a space.
     *
     * @param c the character to be checked.
     * @return true if the character is a space and false otherwise.
     */
    public boolean isSpace(int c) {
        return Character.isWhitespace(c);
    }

    /**
     * Determines if the character is an alphanumeric.
     *
     * @param c the character to be checked.
     * @return true if the character is an alphanumeric and false otherwise.
     */
    public boolean isAlnum(int c) {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    /**
     * Determines if the character is an alphanumeric or an underscore.
     *
     * @param c the character to be checked.
     * @return true if the character is an alphanumeric or an underscore and false otherwise.
     */
    public boolean isAlnumUnderscore(int c) {
        return isAlnum(c) || c == '_';
    }

    /**
     * Determines if the character is a valid special character.
     *
     * @param c the character to be checked.
     * @return true if the character is a valid special character and false otherwise.
     */
    public boolean isSpecialChar(int c) {
        return SPECIAL_CHARS.indexOf(c) >= 0;
    }

    /**
     * Determines if the character is a valid separator.
     *
     * @param c the character to be checked.
     * @return true if the character is a valid separator and false otherwise.
     */
    public boolean isSep(int c) {
        return c == EOS || isSpace(c) || c == ';' || c == ',';
    }

    /**
     * Gets the current position in the source.
     *
     * @return a SrcPos object as the current source position.
     */
    public SrcPos getSrcPos() {
        int ln = lnEndList.size();
        int col = lnEndList.get(lnEndList.size() - 1);
        return new SrcPos(ln, col);
    }

    /**
     * Skips the spaces until a non-space character is encountered.
     *
     * @throws IOException if the read operation causes an IO error.
     */
    public void skipSpaces() throws IOException {
        int c;
        while ((c = peek()) != EOS && isSpace(c)) {
            read();
        }
    }

    /**
     * Skips a single-line comment, that is, until a newline character is encountered.
     *
     * @throws IOException if the read operation causes an IO error.
     */
    public void skipSinglelineComment() throws IOException {
        int c;
        if ((c = peek()) == LexReader.EOS || c != '/') {
            return;
        }

        read();
        if ((c = peek()) == LexReader.EOS || c != '/') {
            putBack("/");
            return;
        }

        read();
        while ((c = read()) != LexReader.EOS && c != '\n') ;
    }

    /**
     * Skips a multiline comment.
     *
     * @throws IOException if the read operation causes an IO error.
     */
    public void skipMultilineComment() throws IOException {
        int c;
        if ((c = peek()) == LexReader.EOS || c != '/') {
            return;
        }

        read();
        if ((c = peek()) == LexReader.EOS || c != '*') {
            putBack("/");
            return;
        }

        read();
        boolean end = false;

        while (!end) {
            c = read();
            end = c == LexReader.EOS;
            if (!end && c == '*') {
                c = read();
                end = c == LexReader.EOS || c == '/';
            }
        }
    }

    /**
     * Peeks without extracting a character from the internal buffer. If the internal buffer is empty, read a character
     * from the stream into the buffer.
     *
     * @return the peeked character(as an int).
     * @throws IOException if there is an error while reading from the stream.
     */
    public int peek() throws IOException {
        if (buff.isEmpty()) {
            buff.addLast(reader.read());
        }
        // This buffer will never be empty
        assert !buff.isEmpty();
        return buff.peekFirst();
    }

    /**
     * Peeks and extracts the first character in the internal buffer.
     *
     * @return the extracted character if there is any.
     * @throws IOException if there is an error while reading from the stream.
     */
    public int read() throws IOException {
        peek();
        int c = buff.pop();

        if (c == '\n') {
            lnEndList.add(1);
        } else {
            int currLnEnd = lnEndList.size() - 1;
            lnEndList.set(currLnEnd, lnEndList.get(currLnEnd) + 1);
        }

        return c;
    }

    /**
     * Puts back a valid string into the internal buffer.
     *
     * @param str the string to be put back.
     */
    public void putBack(String str) {
        int c, currLnEnd;
        for (int i = str.length() - 1; i >= 0; --i) {
            c = str.charAt(i);
            currLnEnd = lnEndList.size() - 1;

            if (c == '\n') {
                lnEndList.remove(currLnEnd);
            } else {
                lnEndList.set(currLnEnd, lnEndList.get(currLnEnd) - 1);
            }

            buff.addFirst(c);
        }
    }
}
