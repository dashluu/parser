package parse.decl_stmt;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import parse.dtype.DtypeSemanChecker;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.VarInfo;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;

public class DeclStmtSemanChecker {
    private ParseContext context;
    private DtypeSemanChecker dtypeSemanChecker;

    /**
     * Initializes the dependencies.
     *
     * @param dtypeSemanChecker a data type semantic checker.
     */
    public void init(DtypeSemanChecker dtypeSemanChecker) {
        this.dtypeSemanChecker = dtypeSemanChecker;
    }

    /**
     * Checks the semantics of a variable declaration statement.
     *
     * @param root    the variable declaration statement AST's root.
     * @param context the parsing context.
     * @return a ParseResult object as the result of checking the variable declaration statement's semantics.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode root, ParseContext context) {
        this.context = context;
        ParseResult<SymbolInfo> declResult;
        VarDeclASTNode declNode;
        ASTNodeType rootNodeType = root.getNodeType();
        if (rootNodeType == ASTNodeType.VAR_DECL) {
            // Variable declaration without rhs expression
            declNode = (VarDeclASTNode) root;
            declResult = checkVarDecl(declNode);
            if (declResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            return ParseResult.ok(root);
        }

        VarDefASTNode defNode = (VarDefASTNode) root;
        declNode = defNode.getVarDeclNode();
        declResult = checkVarDecl(declNode);
        if (declResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        return typeCheckVarDef(declResult.getData(), defNode);
    }

    /**
     * Checks a variable declaration, that is, its identifier and data type.
     *
     * @param declNode the AST node associated with the variable declaration.
     * @return a ParseResult object as the result of checking the variable declaration.
     */
    private ParseResult<SymbolInfo> checkVarDecl(VarDeclASTNode declNode) {
        IdASTNode idNode = declNode.getIdNode();
        ParseResult<SymbolInfo> idResult = checkId(idNode);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        SymbolInfo idSymbol = idResult.getData();
        DtypeASTNode dtypeNode = declNode.getDtypeNode();
        if (dtypeNode == null) {
            return ParseResult.ok(idSymbol);
        }

        ParseResult<ASTNode> dtypeResult = dtypeSemanChecker.checkDtype(dtypeNode, context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        TypeInfo dtype = dtypeResult.getData().getDtype();
        declNode.setDtype(dtype);
        idNode.setDtype(dtype);
        return ParseResult.ok(idSymbol);
    }

    /**
     * Checks if a variable declaration identifier is valid.
     *
     * @param idNode the AST node containing the variable declaration identifier.
     * @return a ParseResult object as the result of checking the variable declaration identifier.
     */
    private ParseResult<SymbolInfo> checkId(IdASTNode idNode) {
        Tok idTok = idNode.getTok();
        String id = idTok.getVal();
        // Check if the declaration id is a data type since the id cannot be a keyword
        TypeInfo dtype = context.getTypeTable().getType(id);
        if (dtype != null) {
            return context.raiseErr(new ErrMsg("Data type '" + id + "' cannot be used as a variable identifier", idTok));
        }

        // Check if the declaration id has been defined
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getLocalSymbol(id);
        if (symbol != null) {
            return context.raiseErr(new ErrMsg("Identifier '" + id + "' cannot be redeclared", idTok));
        }

        // Create a new symbol
        symbol = new VarInfo(id, null, idNode.isMutable());
        symbolTable.registerSymbol(symbol);
        return ParseResult.ok(symbol);
    }

    /**
     * Checks type compatibility between the left-hand side and right-hand side of the variable definition.
     *
     * @param varSymbol the symbol on the left-hand side.
     * @param defNode   the AST node associated with the variable definition.
     * @return a ParseResult object as the result of type checking both sides of the variable definition.
     */
    private ParseResult<ASTNode> typeCheckVarDef(SymbolInfo varSymbol, VarDefASTNode defNode) {
        Tok defTok = defNode.getTok();
        VarDeclASTNode declNode = defNode.getVarDeclNode();
        ASTNode exprNode = defNode.getExprNode();
        TypeInfo lhsDtype = declNode.getDtype();
        TypeInfo rhsDtype = exprNode.getDtype();
        TypeInfo resultDtype;

        // Compare and check if the lhs and rhs have compatible types
        if (rhsDtype == null) {
            return context.raiseErr(new ErrMsg("No type detected on the right-hand side", defTok));
        } else if (lhsDtype == null || lhsDtype.equals(rhsDtype)) {
            resultDtype = rhsDtype;
        } else {
            OpCompat opCompat = new BinOpCompat(TokType.ASSIGNMENT, lhsDtype, rhsDtype);
            resultDtype = context.getOpTable().getCompatDtype(opCompat);
            if (resultDtype == null) {
                return context.raiseErr(new ErrMsg("Unable to assign data of type '" + rhsDtype.getId() +
                        "' to data of type '" + lhsDtype.getId() + "'", defTok));
            }
        }

        varSymbol.setDtype(resultDtype);
        // Only assign the result data type to the definition node
        // No need to do so to the id node or the declaration node
        defNode.setDtype(resultDtype);
        return ParseResult.ok(defNode);
    }
}
