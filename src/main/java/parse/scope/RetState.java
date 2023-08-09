package parse.scope;

// States for determining whether a scope contains a return statement
public enum RetState {
    INIT, CHECKING, MISSING, EXIST
}
