package parsers.utils;

import toks.Tok;

public class ParseResult<T> {
    private final T data;
    private final ParseStatus status;
    private final Tok failTok;

    protected ParseResult(T data, ParseStatus status, Tok failTok) {
        this.data = data;
        this.status = status;
        this.failTok = failTok;
    }

    public static <E> ParseResult<E> ok(E data) {
        return new ParseResult<>(data, ParseStatus.OK, null);
    }

    public static <E> ParseResult<E> fail(Tok failTok) {
        return new ParseResult<>(null, ParseStatus.FAIL, failTok);
    }

    public static <E> ParseResult<E> empty() {
        return new ParseResult<>(null, ParseStatus.EMPTY, null);
    }

    public static <E> ParseResult<E> err() {
        return new ParseResult<>(null, ParseStatus.ERR, null);
    }

    public T getData() {
        return data;
    }

    public ParseStatus getStatus() {
        return status;
    }

    public Tok getFailTok() {
        return failTok;
    }
}
