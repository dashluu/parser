package parse.decl_stmt;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
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

        ParseResult<TypeInfo> dtypeResult = checkDtype(dtypeNode);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        TypeInfo dtype = dtypeResult.getData();
        declNode.setDtype(dtype);
        idNode.setDtype(dtype);
        dtypeNode.setDtype(dtype);
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
     * Checks if a data type is valid.
     *
     * @param dtypeNode the AST node that stores a data type token.
     * @return a ParseResult object as the result of checking the data type.
     */
    private ParseResult<TypeInfo> checkDtype(DtypeASTNode dtypeNode) {
        Tok dtypeTok = dtypeNode.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = context.getTypeTable().getType(dtypeId);
        if (dtype == null) {
            return context.raiseErr(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }
        return ParseResult.ok(dtype);
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

        // Compare and check if the lhs and rhs have compatible types
        if (rhsDtype == null) {
            return context.raiseErr(new ErrMsg("No type detected on the right-hand side", defTok));
        } else if (lhsDtype != rhsDtype) {
            if (lhsDtype != null) {
                OpCompat opCompat = new BinOpCompat(TokType.ASSIGNMENT, lhsDtype, rhsDtype);
                if (context.getOpTable().getCompatDtype(opCompat) == null) {
                    return context.raiseErr(new ErrMsg("Unable to assign data of type '" + rhsDtype.getId() +
                            "' to data of type '" + lhsDtype.getId() + "'", defTok));
                }
            }
            declNode.setDtype(rhsDtype);
            varSymbol.setDtype(rhsDtype);
        }

        defNode.setDtype(rhsDtype);
        return ParseResult.ok(defNode);
    }
}
