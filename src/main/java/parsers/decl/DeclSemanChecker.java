package parsers.decl;

import ast.ASTNode;
import ast.ASTNodeType;
import ast.BinASTNode;
import exceptions.ErrMsg;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.Scope;
import parsers.expr.ExprSemanChecker;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import toks.Tok;
import types.TypeInfo;

import java.io.IOException;

public class DeclSemanChecker {
    private ExprSemanChecker exprSemanChecker;
    private final ParseErr err = ParseErr.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param exprSemanChecker an object that checks the right-hand side(rhs) expression's semantics.
     */
    public void init(ExprSemanChecker exprSemanChecker) {
        this.exprSemanChecker = exprSemanChecker;
    }

    /**
     * Checks the semantics of a declaration statement.
     *
     * @param declNode the declaration AST's root.
     * @param scope    the scope surrounding the declaration.
     * @return a ParseResult object as the result of checking the declaration statement's semantics.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode declNode, Scope scope) throws IOException {
        if (declNode.getNodeType() != ASTNodeType.DEF) {
            return ParseResult.ok(declNode);
        }
        BinASTNode asgnmtNode = (BinASTNode) declNode;
        ASTNode exprNode = asgnmtNode.getRight();
        // Check the semantics of the rhs expression
        ParseResult<ASTNode> exprResult = exprSemanChecker.checkSeman(exprNode, scope);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        }
        return typeCheckDef(asgnmtNode, scope);
    }

    /**
     * Checks type compatibility between the left-hand side and right-hand side of a declaration statement.
     *
     * @param asgmtNode the declaration AST's root.
     * @param scope     the current scope surrounding the declaration statement.
     * @return a ParseResult object as the result of type checking both sides of the declaration.
     */
    private ParseResult<ASTNode> typeCheckDef(BinASTNode asgmtNode, Scope scope) {
        Tok asgmtTok = asgmtNode.getTok();
        ASTNode lhsNode = asgmtNode.getLeft();
        ASTNode exprNode = asgmtNode.getRight();
        TypeInfo lhsDtype = lhsNode.getDtype();
        TypeInfo rhsDtype = exprNode.getDtype();

        // Compare and check if the lhs and rhs have the same data type
        if (rhsDtype == null) {
            return err.raise(new ErrMsg("No type detected on the right-hand side", asgmtTok));
        } else if (lhsDtype != rhsDtype) {
            if (lhsDtype != null) {
                return err.raise(new ErrMsg("Unable to assign data of type '" + rhsDtype.id() +
                        "' to data of type '" + lhsDtype.id() + "'", asgmtTok));
            } else {
                // If the lhs's data type is null, we assign rhs's data type directly to lhs
                lhsNode.setDtype(rhsDtype);
                // Set the variable or constant's data type in the symbol table
                SymbolTable symbolTable = scope.getSymbolTable();
                String id = lhsNode.getTok().getVal();
                SymbolInfo symbol = symbolTable.getSymbol(id);
                symbol.setDtype(rhsDtype);
            }
        }

        asgmtNode.setDtype(rhsDtype);
        return ParseResult.ok(asgmtNode);
    }
}
