package exceptions;

import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;

public class ErrMsg {
    private final String val;
    private SrcPos srcPos;

    public ErrMsg(String val, SrcPos srcPos) {
        this.val = val;
        this.srcPos = srcPos;
    }

    public ErrMsg(String val, SrcRange srcRange) {
        this(val, srcRange.getStartPos());
    }

    public ErrMsg(String val, Tok tok) {
        this(val, tok.getSrcRange().getStartPos());
    }

    public String getVal() {
        return val;
    }

    public SrcPos getSrcPos() {
        return srcPos;
    }

    public void setSrcPos(SrcPos srcPos) {
        this.srcPos = srcPos;
    }
}
