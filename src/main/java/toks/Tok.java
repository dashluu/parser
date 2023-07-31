package toks;

public class Tok {
    private final String val;
    private TokType type;
    private SrcRange srcRange;

    public Tok(String val, TokType type, SrcRange srcRange) {
        this.val = val;
        this.type = type;
        this.srcRange = srcRange;
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

    public SrcRange getSrcRange() {
        return srcRange;
    }

    public void setSrcRange(SrcRange srcRange) {
        this.srcRange = srcRange;
    }

    @Override
    public String toString() {
        return "token: " + val + ", token type: " + type + ", " + srcRange;
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
