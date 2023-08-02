package parsers.decl;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.VarInfo;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;

public class DeclSemanChecker {
    private ParseContext context;

    /**
     * Checks the semantics of a variable declaration or definition.
     *
     * @param root    the variable declaration or definition AST's root.
     * @param context the parsing context.
     * @return a ParseResult object as the result of checking the variable declaration or definition's semantics.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode root, ParseContext context) {
        this.context = context;
        ParseResult<SymbolInfo> varDeclResult;
        VarDeclASTNode varDeclNode;
        ASTNodeType rootNodeType = root.getNodeType();
        if (rootNodeType == ASTNodeType.VAR_DECL) {
            // Variable declaration without rhs expression
            varDeclNode = (VarDeclASTNode) root;
            varDeclResult = checkVarDecl(varDeclNode);
            if (varDeclResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            return ParseResult.ok(root);
        }

        BinASTNode asgnmtNode = (BinASTNode) root;
        varDeclNode = (VarDeclASTNode) asgnmtNode.getLeft();
        varDeclResult = checkVarDecl(varDeclNode);
        if (varDeclResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        return typeCheckAsgnmt(varDeclResult.getData(), asgnmtNode);
    }

    /**
     * Checks a variable declaration, that is, its identifier and data type.
     *
     * @param varDeclNode the AST node associated with the variable declaration.
     * @return a ParseResult object as the result of checking the variable declaration.
     */
    private ParseResult<SymbolInfo> checkVarDecl(VarDeclASTNode varDeclNode) {
        IdASTNode idNode = varDeclNode.getIdNode();
        ParseResult<SymbolInfo> idResult = checkId(idNode);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        DtypeASTNode dtypeNode = varDeclNode.getDtypeNode();
        ParseResult<TypeInfo> dtypeResult = checkDtype(dtypeNode);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        TypeInfo dtype = dtypeResult.getData();
        varDeclNode.setDtype(dtype);
        idNode.setDtype(dtype);
        dtypeNode.setDtype(dtype);
        return ParseResult.ok(idResult.getData());
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
     * Checks type compatibility between the left-hand side and right-hand side of the assignment, or the definition.
     *
     * @param varSymbol  the symbol on the left-hand side.
     * @param asgnmtNode the declaration AST's root.
     * @return a ParseResult object as the result of type checking both sides of the assignment.
     */
    private ParseResult<ASTNode> typeCheckAsgnmt(SymbolInfo varSymbol, BinASTNode asgnmtNode) {
        Tok asgmtTok = asgnmtNode.getTok();
        ASTNode varDeclNode = asgnmtNode.getLeft();
        ASTNode exprNode = asgnmtNode.getRight();
        TypeInfo lhsDtype = varDeclNode.getDtype();
        TypeInfo rhsDtype = exprNode.getDtype();

        // Compare and check if the lhs and rhs have compatible types
        if (rhsDtype == null) {
            return context.raiseErr(new ErrMsg("No type detected on the right-hand side", asgmtTok));
        } else if (lhsDtype != rhsDtype) {
            if (lhsDtype != null) {
                OpCompat opCompat = new BinOpCompat(TokType.ASSIGNMENT, lhsDtype, rhsDtype);
                if (context.getOpTable().getCompatDtype(opCompat) == null) {
                    return context.raiseErr(new ErrMsg("Unable to assign data of type '" + rhsDtype.getId() +
                            "' to data of type '" + lhsDtype.getId() + "'", asgmtTok));
                }
            }
            varDeclNode.setDtype(rhsDtype);
            varSymbol.setDtype(rhsDtype);
        }

        asgnmtNode.setDtype(rhsDtype);
        return ParseResult.ok(asgnmtNode);
    }
}
