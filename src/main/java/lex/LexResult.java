package lex;

import exceptions.ErrMsg;

public class LexResult<T> {
    private final LexStatus status;
    private final T data;
    private final ErrMsg errMsg;

    private LexResult(LexStatus status, T data, ErrMsg errMsg) {
        this.status = status;
        this.data = data;
        this.errMsg = errMsg;
    }

    public static <E> LexResult<E> ok(E data) {
        return new LexResult<>(LexStatus.OK, data, null);
    }

    public static <E> LexResult<E> fail() {
        return new LexResult<>(LexStatus.FAIL, null, null);
    }

    public static <E> LexResult<E> err(ErrMsg errMsg) {
        return new LexResult<>(LexStatus.ERR, null, errMsg);
    }

    public LexStatus getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public ErrMsg getErrMsg() {
        return errMsg;
    }
}
