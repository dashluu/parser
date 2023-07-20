package parsers.parse_utils;

import toks.Tok;

public class ParseResult<T> {
    // The data that is stored if parsed successfully
    private final T data;
    // The parsing status
    private final ParseStatus status;
    // The token where parsing fails(if any)
    private final Tok failTok;

    protected ParseResult(T data, ParseStatus status, Tok failTok) {
        this.data = data;
        this.status = status;
        this.failTok = failTok;
    }

    /**
     * Creates and returns a ParseResult object that indicates success.
     *
     * @param data the data to be stored.
     * @param <E>  the type of the stored data.
     * @return a ParseResult object.
     */
    public static <E> ParseResult<E> ok(E data) {
        return new ParseResult<>(data, ParseStatus.OK, null);
    }

    /**
     * Creates and returns a ParseResult object that indicates failure.
     *
     * @param failTok the token where parsing fails.
     * @param <E>     the parameterized type.
     * @return a ParseResult object.
     */
    public static <E> ParseResult<E> fail(Tok failTok) {
        return new ParseResult<>(null, ParseStatus.FAIL, failTok);
    }

    /**
     * Creates and returns a ParseResult object that indicates success but empty result.
     *
     * @param <E> the parameterized type.
     * @return a ParseResult object.
     */
    public static <E> ParseResult<E> empty() {
        return new ParseResult<>(null, ParseStatus.EMPTY, null);
    }

    /**
     * Creates and returns a ParseResult object that indicates error.
     *
     * @param <E> the parameterized type.
     * @return a ParseResult object.
     */
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
