package parsers.utils;

import exceptions.ErrMsg;

public class ParseErr {
    private ErrMsg msg;
    private final static ParseErr INSTANCE = new ParseErr();

    private ParseErr() {
    }

    public static ParseErr getInst() {
        return INSTANCE;
    }

    /**
     * Updates the error to the earliest one as possible.
     *
     * @param aMsg the error message.
     */
    public void update(ErrMsg aMsg) {
        if (msg == null || msg.getRow() > aMsg.getRow() || msg.getCol() > aMsg.getCol()) {
            msg = aMsg;
        }
    }

    /**
     * Updates the error to the earliest one as possible and also returns a signal.
     *
     * @param msg the error message
     * @param <E> type argument to the ParseResult object.
     * @return a ParseResult object as an error signal.
     */
    public <E> ParseResult<E> raise(ErrMsg msg) {
        update(msg);
        return ParseResult.err();
    }

    public ErrMsg getMsg() {
        return msg;
    }

    public boolean hasErr() {
        return msg != null;
    }
}
