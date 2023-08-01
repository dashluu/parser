package toks;

public class SrcRange {
    private SrcPos startPos;
    private SrcPos endPos;

    public SrcRange(SrcPos startPos, SrcPos endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public SrcRange(SrcPos pos) {
        this(pos, pos);
    }

    public SrcPos getStartPos() {
        return startPos;
    }

    public void setStartPos(SrcPos startPos) {
        this.startPos = startPos;
    }

    public SrcPos getEndPos() {
        return endPos;
    }

    public void setEndPos(SrcPos endPos) {
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        return "ln " + startPos.getLn() + ", col: " + startPos.getCol() +
                " - ln " + endPos.getLn() + ", col: " + endPos.getCol();
    }
}
