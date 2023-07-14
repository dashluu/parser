package parsers.utils;

import toks.Tok;

// A class for storing syntax information during the first pass of parsing
public class SyntaxInfo {
    private final Tok tok;
    private SyntaxTag tag;

    public SyntaxInfo(Tok tok, SyntaxTag tag) {
        this.tok = tok;
        this.tag = tag;
    }

    public Tok getTok() {
        return tok;
    }

    public void setTag(SyntaxTag tag) {
        this.tag = tag;
    }

    public SyntaxTag getTag() {
        return tag;
    }
}
