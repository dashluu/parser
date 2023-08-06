package toks;

public class Tok {
    private final String val;
    private TokType tokType;
    private SrcRange srcRange;

    public Tok(String val, TokType tokType, SrcRange srcRange) {
        this.val = val;
        this.tokType = tokType;
        this.srcRange = srcRange;
    }

    public String getVal() {
        return val;
    }

    public TokType getTokType() {
        return tokType;
    }

    public void setTokType(TokType tokType) {
        this.tokType = tokType;
    }

    public SrcRange getSrcRange() {
        return srcRange;
    }

    public void setSrcRange(SrcRange srcRange) {
        this.srcRange = srcRange;
    }

    @Override
    public String toString() {
        return "val: " + val + ", tok type: " + tokType + ", " + srcRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tok tok)) {
            return false;
        }
        return val.equals(tok.getVal()) && tokType == tok.tokType;
    }
}
