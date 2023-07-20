package exceptions;

import toks.Tok;

public class ErrMsg {
    private final String val;
    private final int row;
    private int col;

    public ErrMsg(String val, int row, int col) {
        this.val = val;
        this.row = row;
        this.col = col;
    }

    public ErrMsg(String val, Tok tok) {
        this(val, tok.getRow(), tok.getCol());
    }

    public ErrMsg(String val, int row) {
        this(val, row, 1);
    }

    public String getVal() {
        return val;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
