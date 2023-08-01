package lexers;

import exceptions.ErrMsg;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class NumLexer {
    private final LexReader reader;

    public NumLexer(LexReader reader) {
        this.reader = reader;
    }

    /**
     * Reads a string from the stream to check if it matches the given string. Otherwise, it puts everything that has
     * been read back to the buffer.
     *
     * @param strToMatch the string to match.
     * @return true if the read string matches the given string and false otherwise.
     * @throws IOException if there is an error while reading.
     */
    private boolean readStr(String strToMatch) throws IOException {
        int c;
        int i = 0;
        boolean matched = true;
        StringBuilder strBuff = new StringBuilder();
        while (i < strToMatch.length() && (c = reader.peek()) != LexReader.EOS && matched) {
            matched = (char) c == strToMatch.charAt(i);
            if (matched) {
                // If the current character matches, update the temp buffer
                strBuff.append((char) c);
                reader.read();
                ++i;
            }
        }
        if (strToMatch.contentEquals(strBuff)) {
            return true;
        }
        // Put back what has been read if what's in the buffer does not match the expected string
        reader.putBack(strBuff.toString());
        return false;
    }

    /**
     * Reads a sequence of digits.
     * Grammar: ('0'-'9')+
     *
     * @return a string containing a sequence of digits if it exists, otherwise, return null.
     * @throws IOException if the read operation causes an error.
     */
    private String readDigits() throws IOException {
        int c;
        StringBuilder digits = new StringBuilder();
        while ((c = reader.peek()) != LexReader.EOS && Character.isDigit(c)) {
            digits.append((char) c);
            reader.read();
        }
        if (digits.isEmpty()) {
            return null;
        }
        return digits.toString();
    }

    /**
     * Reads the fraction part in a numeric expression.
     *
     * @param fracOpt true if the fraction is optional and false otherwise.
     * @return a string containing the fraction and null otherwise.
     * @throws IOException if the read operation causes an error.
     */
    private String readFrac(boolean fracOpt) throws IOException {
        if (!readStr(".")) {
            return null;
        }
        String digits = readDigits();
        if (digits == null) {
            if (fracOpt) {
                digits = "0";
            } else {
                reader.putBack(".");
                return null;
            }
        }
        return "." + digits;
    }

    /**
     * Reads the optional exponent in a numeric expression.
     *
     * @return a LexResult object as the result of reading an optional exponent.
     * @throws IOException if the read operation causes an error.
     */
    private LexResult<String> readExp() throws IOException {
        // Read e
        if (!readStr("e")) {
            return LexResult.fail();
        }
        // Read +/-
        StringBuilder exp = new StringBuilder("e");
        if (readStr("+")) {
            exp.append("+");
        } else if (readStr("-")) {
            exp.append("-");
        }
        // Read digits
        String digits = readDigits();
        if (digits == null) {
            return LexResult.err(new ErrMsg("Expected a sequence of digits after '" +
                    exp.charAt(exp.length() - 1) + "'", reader.getSrcPos()));
        }
        exp.append(digits);
        return LexResult.ok(exp.toString());
    }

    /**
     * Reads a numeric expression.
     *
     * @return a LexResult object as the result of reading a numeric expression.
     * @throws IOException if the read operation causes an error.
     */
    public LexResult<Tok> read() throws IOException {
        StringBuilder tokVal = new StringBuilder();
        SrcPos startPos = reader.getSrcPos();

        // Read sequence of digits
        String digits = readDigits();
        boolean hasDigits = digits != null;
        if (hasDigits) {
            tokVal.append(digits);
        } else {
            tokVal.append("0");
        }

        // Read fraction part
        String frac = readFrac(hasDigits);
        boolean isFp = frac != null;
        if (isFp) {
            tokVal.append(frac);
        } else if (!hasDigits) {
            return LexResult.fail();
        }

        // Read optional exponent
        LexResult<String> expResult = readExp();
        if (expResult.getStatus() == LexStatus.OK) {
            tokVal.append(expResult.getData());
        } else if (expResult.getStatus() == LexStatus.ERR) {
            return LexResult.err(expResult.getErrMsg());
        }

        TokType literalType = (isFp ? TokType.FLOAT_LITERAL : TokType.INT_LITERAL);
        SrcPos endPos = reader.getSrcPos();
        SrcRange srcRange = new SrcRange(startPos, endPos);
        Tok numTok = new Tok(tokVal.toString(), literalType, srcRange);
        return LexResult.ok(numTok);
    }
}
