package parsers.scope;

import java.util.ArrayDeque;

public class ScopeStack {
    private final ArrayDeque<Scope> stack = new ArrayDeque<>();

    /**
     * Pushes a new scope onto the stack.
     *
     * @param scope the new scope to be pushed.
     */
    public void push(Scope scope) {
        stack.push(scope);
    }

    /**
     * Pops the top scope off the stack and updates the return state of the parent scope if necessary.
     *
     * @return the scope that is popped off the stack.
     */
    public Scope pop() {
        Scope scope = stack.pop();
        Scope parent = stack.peek();

        // Update the return state of the parent scope
        if (parent != null && parent.getRetState() != RetState.MISSING && parent.getRetState() != RetState.PRESENT) {
            if (scope.getScopeType() == ScopeType.SIMPLE || scope.getScopeType() == ScopeType.ELSE) {
                if (scope.getRetState() == RetState.PRESENT) {
                    parent.setRetState(RetState.PRESENT);
                }
            } else {
                if (scope.getRetState() == RetState.PRESENT) {
                    parent.setRetState(RetState.CHECKING);
                } else {
                    parent.setRetState(RetState.MISSING);
                }
            }
        }

        return scope;
    }

    /**
     * Gets the scope on top of the stack.
     *
     * @return a Scope object.
     */
    public Scope peek() {
        return stack.peek();
    }
}
