package exceptions;

public class SyntaxErr extends Exception {
    public SyntaxErr(ErrMsg errMsg) {
        super(errMsg.getVal() + " on " + errMsg.getSrcPos());
    }
}
