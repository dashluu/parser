package parsers.utils;

// A tag to assign to a SyntaxInfo object used in parsing
public enum SyntaxTag {
    PREFIX, POSTFIX, INFIX, LPAREN, RPAREN, ID, LITERAL,
    VAR_DECL, CONST_DECL, TYPE_ID, ASGNMT,
    FUN_CALL, COMMA,
    FUN_DEF, PARAM,
    END
}
