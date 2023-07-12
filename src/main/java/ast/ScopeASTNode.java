package ast;

import parsers.utils.MemTable;

// Node for code scopes
public class ScopeASTNode extends KnaryASTNode {
    private final MemTable memTable;
    private boolean retFlag;

    public ScopeASTNode(MemTable memTable) {
        super(null, ASTNodeType.SCOPE, null);
        this.memTable = memTable;
        retFlag = false;
    }

    public MemTable getMemTable() {
        return memTable;
    }

    public boolean getRetFlag() {
        return retFlag;
    }

    public void setRetFlag(boolean retFlag) {
        this.retFlag = retFlag;
    }

    @Override
    public String toJsonStr() {
        return super.toJsonStr() + ",\"Return flag\":\"" + retFlag + "\"";
    }

    @Override
    public void accept(IASTVisitor visitor) {
        visitor.visitScope(this);
    }
}
