package parsers.decl;

import ast.*;
import exceptions.ErrMsg;
import parsers.expr.ExprASTPass;
import parsers.utils.*;
import symbols.*;
import toks.Tok;
import types.TypeInfo;
import types.TypeTable;

public class DeclASTPass {
    private SyntaxBuff syntaxBuff;
    private Scope scope;
    private ExprASTPass exprASTPass;
    private static final ParseErr err = ParseErr.getInst();
    private static final TypeTable typeTable = TypeTable.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param exprASTPass an object that constructs the right-hand side(rhs) expression's AST.
     */
    public void init(ExprASTPass exprASTPass) {
        this.exprASTPass = exprASTPass;
    }

    /**
     * Constructs a partial AST for a declaration statement.
     *
     * @param scope the scope surrounding the declaration statement.
     * @return a ParseResult object as the result of constructing the partial AST.
     */
    public ParseResult<ASTNode> doDecl(SyntaxBuff syntaxBuff, Scope scope) {
        this.syntaxBuff = syntaxBuff;
        this.scope = scope;

        // If the cursor isn't at the front, there is an assignment
        boolean hasAsgnmt = !syntaxBuff.atFront();

        // Construct AST for expression first since the new symbol on lhs should not be initialized before
        // the expression is finished parsing.
        ParseResult<ASTNode> exprResult = null;
        if (hasAsgnmt) {
            // Process expression only if there is an assignment
            exprResult = exprASTPass.doExpr(syntaxBuff, scope);
            // Move the cursor to front of the buffer to parse lhs
            syntaxBuff.toFront();
        }

        ParseResult<ASTNode> lhsResult = doLhs();
        if (err.hasErr()) {
            // This error can be propagated from either the lhs or the expression
            return ParseResult.err();
        } else if (!hasAsgnmt) {
            // No assignment, that is, no definition
            return lhsResult;
        }

        ASTNode lhsNode = lhsResult.getData();
        ASTNode exprNode = exprResult.getData();
        boolean mutable = lhsNode.getNodeType() == ASTNodeType.VAR_DECL;
        ParseResult<ASTNode> asgnmtResult = doAsgnmt(mutable);
        BinASTNode asgnmtNode = (BinASTNode) asgnmtResult.getData();
        asgnmtNode.setLeft(lhsNode);
        asgnmtNode.setRight(exprNode);
        return ParseResult.ok(asgnmtNode);
    }

    /**
     * Constructs an AST for the left-hand side(lhs) of a declaration statement.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doLhs() {
        // Pop head directly
        SyntaxInfo headInfo = syntaxBuff.forward();
        boolean mutable = headInfo.getTag() == SyntaxTag.VAR_DECL;

        // Pop id directly
        SyntaxInfo idInfo = syntaxBuff.forward();
        Tok idTok = idInfo.getTok();
        String id = idTok.getVal();
        SymbolTable symbolTable = scope.getSymbolTable();
        SymbolInfo symbol = symbolTable.getSymbol(id);
        if (symbol != null) {
            return err.raise(new ErrMsg("'" + id + "' cannot be redeclared", idTok));
        }

        // Add a variable or a constant to the symbol table
        symbol = mutable ? new VarInfo(id, null) : new ConstInfo(id, null);
        symbolTable.registerSymbol(symbol);
        // Try processing data type
        TypeInfo dtype = null;
        SyntaxInfo dtypeInfo = syntaxBuff.peek();
        if (dtypeInfo.getTag() == SyntaxTag.TYPE_ID) {
            Tok dtypeTok = dtypeInfo.getTok();
            String dtypeId = dtypeTok.getVal();
            dtype = typeTable.getType(dtypeId);
            if (dtype == null) {
                return err.raise(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
            }
            syntaxBuff.forward();
        }

        ASTNode idNode = mutable ? new VarDeclASTNode(idTok, dtype) : new ConstDeclASTNode(idTok, dtype);
        return ParseResult.ok(idNode);
    }

    /**
     * Constructs an AST node for the assignment operator.
     *
     * @param mutable boolean value for declaration's mutability.
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doAsgnmt(boolean mutable) {
        SyntaxInfo syntaxInfo = syntaxBuff.peek();
        if (syntaxInfo.getTag() != SyntaxTag.ASGNMT) {
            return ParseResult.fail(syntaxInfo.getTok());
        }

        Tok asgnmtTok = syntaxInfo.getTok();
        ASTNode asgnmtNode = mutable ?
                new VarDefASTNode(asgnmtTok, null) :
                new ConstDefASTNode(asgnmtTok, null);
        return ParseResult.ok(asgnmtNode);
    }
}
