package ast;

public interface IASTNodeIterator {
    boolean hasNext();

    ASTNode next();

    void set(ASTNode node);
}
