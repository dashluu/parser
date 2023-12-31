package toks;

public class SrcPos {
    private int ln;
    private int col;

    public SrcPos(int ln, int col) {
        this.ln = ln;
        this.col = col;
    }

    public SrcPos(SrcPos srcPos) {
        this(srcPos.ln, srcPos.col);
    }

    public SrcPos() {
        this(1, 1);
    }

    public int getLn() {
        return ln;
    }

    public void setLn(int ln) {
        this.ln = ln;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return "line " + ln + ", column " + col;
    }
}
