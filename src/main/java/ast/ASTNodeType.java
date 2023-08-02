package ast;

public enum ASTNodeType {
    LITERAL, ID, VAR_DECL, BIN_OP, UN_OP, VAR_DEF, DTYPE, SCOPE, FUN_CALL, FUN_DEF, FUN_SIGN,
    PARAM_LIST, PARAM_DECL, RET, IF_ELSE, IF, ELSE, WHILE, TYPE_ANN, BREAK, CONT, ARR_LITERAL,
    ARR_ACCESS, EXPR_LIST
}
