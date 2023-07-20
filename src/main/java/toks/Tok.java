package toks;

public class Tok {
    private final String val;
    private TokType type;
    private final int row;
    private int col;

    public Tok(String val, TokType type, int row, int col) {
        this.val = val;
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public Tok(String val, TokType type, int row) {
        this(val, type, row, 1);
    }

    public Tok(String val, TokType type) {
        this(val, type, 1, 1);
    }

    public String getVal() {
        return val;
    }

    public TokType getType() {
        return type;
    }

    public void setType(TokType type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "token: " + val + ", token type: " + type +
                ", row number: " + row + ", column number: " + col;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tok tok)) {
            return false;
        }
        return val.equals(tok.getVal()) && type == tok.type;
    }
}
