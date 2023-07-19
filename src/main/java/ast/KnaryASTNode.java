package ast;

import toks.Tok;
import types.TypeInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// AST node with multiple children
// Implemented using the Iterator pattern
public class KnaryASTNode extends ASTNode implements Iterable<ASTNode> {
    protected final List<ASTNode> children = new ArrayList<>();

    public KnaryASTNode(Tok tok, ASTNodeType nodeType, TypeInfo dtype) {
        super(tok, nodeType, dtype);
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

    @Override
    public String toJsonStr() {
        StringBuilder jsonStr = new StringBuilder(super.toJsonStr() + ",\"Children\":[");
        boolean isFirst = true;
        for (ASTNode child : children) {
            if (!isFirst) {
                jsonStr.append(",");
            }
            isFirst = false;
            jsonStr.append("{").append(child.toJsonStr()).append("}");
        }
        jsonStr.append("]");
        return jsonStr.toString();
    }

    @Override
    public Iterator<ASTNode> iterator() {
        return children.iterator();
    }
}
