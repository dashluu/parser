package parsers.scope;

// States for determining whether a scope contains a return statement
public enum RetState {
    INITIAL, CHECKING, MISSING, PRESENT
}
