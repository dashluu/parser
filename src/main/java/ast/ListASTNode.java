package ast;

import toks.SrcPos;
import toks.SrcRange;
import types.TypeInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// AST node with multiple children
// Implemented using the Iterator pattern
public abstract class ListASTNode extends ASTNode implements Iterable<ASTNode> {
    protected final List<ASTNode> children = new ArrayList<>();

    public ListASTNode(SrcRange srcRange, ASTNodeType nodeType, TypeInfo dtype, boolean valFlag) {
        super(null, srcRange, nodeType, dtype, valFlag);
        this.srcRange = srcRange;
    }

    /**
     * Adds a child node to the children list.
     *
     * @param child the child node to be added.
     */
    public void addChild(ASTNode child) {
        children.add(child);
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

    public void updateSrcRange() {
        if (!children.isEmpty()) {
            SrcPos startPos = children.get(0).getSrcRange().getStartPos();
            SrcPos endPos = children.get(children.size() - 1).getSrcRange().getEndPos();
            srcRange = new SrcRange(startPos, endPos);
        }
    }

    @Override
    public Iterator<ASTNode> iterator() {
        return children.iterator();
    }

    public ListIterator<ASTNode> listIterator() {
        return children.listIterator();
    }
}
