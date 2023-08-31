package ast;

import toks.SrcRange;
import types.TypeInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// AST node with multiple children
// Implemented using the Iterator pattern
public abstract class MultichildASTNode extends ASTNode implements Iterable<ASTNode> {
    private class ASTNodeIterator implements IASTNodeIterator {
        private int i;

        public ASTNodeIterator() {
            i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < children.size();
        }

        @Override
        public ASTNode next() {
            return children.get(i++);
        }

        @Override
        public void set(ASTNode node) {
            children.set(i - 1, node);
            srcRange.setEndPos(node.srcRange.getEndPos());
        }
    }

    protected final List<ASTNode> children = new ArrayList<>();

    public MultichildASTNode(ASTNodeType nodeType, TypeInfo dtype) {
        super(null, new SrcRange(), nodeType, dtype);
    }

    /**
     * Adds a child node to the children list.
     *
     * @param child the child node to be added.
     */
    public void addChild(ASTNode child) {
        children.add(child);
        srcRange.setEndPos(child.srcRange.getEndPos());
    }

    /**
     * Counts the number of child nodes.
     *
     * @return an integer as the number of child nodes.
     */
    public int countChildren() {
        return children.size();
    }

    /**
     * Checks if the list of child nodes is empty.
     *
     * @return true if it is empty and false otherwise.
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public Iterator<ASTNode> iterator() {
        return children.iterator();
    }

    public IASTNodeIterator nodeIterator() {
        return new ASTNodeIterator();
    }
}
