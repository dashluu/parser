package utils;

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
     * Pops the top scope off the stack.
     */
    public void pop() {
        stack.pop();
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
