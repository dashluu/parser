package parsers.parse_utils;

import exceptions.ErrMsg;

// A class for storing information about parsing errors
public class ParseErr {
    private static ErrMsg msg;

    /**
     * Resets the error message.
     */
    public static void reset() {
        msg = null;
    }

    /**
     * Updates the error message to the earliest one as possible and also returns an error signal.
     *
     * @param aMsg the error message.
     * @param <E>  type argument to the ParseResult object.
     * @return a ParseResult object as an error signal.
     */
    public static <E> ParseResult<E> raise(ErrMsg aMsg) {
        if (msg == null || msg.getRow() > aMsg.getRow() || msg.getCol() > aMsg.getCol()) {
            // Updates the error to the earliest one as possible, that is, one with <= row and <= column
            msg = aMsg;
        }
        return ParseResult.err();
    }

    public static ErrMsg getMsg() {
        return msg;
    }

    /**
     * Checks if there is an error during parsing.
     *
     * @return true if there is an error and false otherwise.
     */
    public static boolean hasErr() {
        return msg != null;
    }
}
