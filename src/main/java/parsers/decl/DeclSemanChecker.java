package parsers.decl;

import ast.ASTNode;
import ast.ASTNodeType;
import ast.BinASTNode;
import ast.TypeAnnASTNode;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import utils.Context;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import symbols.ConstInfo;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.VarInfo;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;

public class DeclSemanChecker {
    private Context context;

    /**
     * Checks the semantics of a declaration statement.
     *
     * @param declNode the declaration AST's root.
     * @param context  the parsing context.
     * @return a ParseResult object as the result of checking the declaration statement's semantics.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode declNode, Context context) {
        this.context = context;
        ParseResult<SymbolInfo> lhsResult;
        ASTNodeType declNodeType = declNode.getNodeType();
        if (declNodeType == ASTNodeType.TYPE_ANN) {
            // Declaration without rhs expression
            lhsResult = checkTypeAnn((TypeAnnASTNode) declNode);
            if (lhsResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            return ParseResult.ok(declNode);
        }

        BinASTNode asgnmtNode = (BinASTNode) declNode;
        ASTNode lhsNode = asgnmtNode.getLeft();
        if (lhsNode.getNodeType() == ASTNodeType.TYPE_ANN) {
            lhsResult = checkTypeAnn((TypeAnnASTNode) lhsNode);
        } else {
            lhsResult = checkId(lhsNode);
        }

        if (lhsResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        SymbolInfo symbol = lhsResult.getData();
        return typeCheckAsgnmt(symbol, asgnmtNode);
    }

    /**
     * Checks the type annotation of a declaration statement.
     *
     * @param typeAnnNode the type annotation node that contains a node with a symbol's identifier on the left and a
     *                    data type node on the right.
     * @return a ParseResult object as the result of checking the declaration statement's type annotation.
     */
    private ParseResult<SymbolInfo> checkTypeAnn(TypeAnnASTNode typeAnnNode) {
        ASTNode idNode = typeAnnNode.getLeft();
        ParseResult<SymbolInfo> idResult = checkId(idNode);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        ASTNode dtypeNode = typeAnnNode.getDtypeNode();
        ParseResult<TypeInfo> dtypeResult = checkDtype(dtypeNode);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        TypeInfo dtype = dtypeResult.getData();
        typeAnnNode.setDtype(dtype);
        idNode.setDtype(dtype);
        dtypeNode.setDtype(dtype);
        return ParseResult.ok(idResult.getData());
    }

    /**
     * Checks if a declaration identifier is valid.
     *
     * @param idNode the AST node containing the declaration identifier.
     * @return a ParseResult object as the result of checking the declaration identifier.
     */
    private ParseResult<SymbolInfo> checkId(ASTNode idNode) {
        // Check if the declaration id has been defined
        Tok idTok = idNode.getTok();
        String id = idTok.getVal();
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getLocalSymbol(id);
        if (symbol != null) {
            return ParseErr.raise(new ErrMsg("'" + id + "' cannot be redeclared", idTok));
        }

        // Create a new symbol
        symbol = idNode.getNodeType() == ASTNodeType.VAR_DECL ?
                new VarInfo(id, null) :
                new ConstInfo(id, null);
        symbolTable.registerSymbol(symbol);
        return ParseResult.ok(symbol);
    }

    /**
     * Checks if a data type is valid.
     *
     * @param dtypeNode the AST node that stores a data type token.
     * @return a ParseResult object as the result of checking a data type.
     */
    private ParseResult<TypeInfo> checkDtype(ASTNode dtypeNode) {
        Tok dtypeTok = dtypeNode.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = context.getTypeTable().getType(dtypeId);
        if (dtype == null) {
            return ParseErr.raise(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }
        return ParseResult.ok(dtype);
    }

    /**
     * Checks type compatibility between the left-hand side and right-hand side of the assignment, or the definition.
     *
     * @param lhsSymbol  the symbol on the left-hand side.
     * @param asgnmtNode the declaration AST's root.
     * @return a ParseResult object as the result of type checking both sides of the assignment.
     */
    private ParseResult<ASTNode> typeCheckAsgnmt(SymbolInfo lhsSymbol, BinASTNode asgnmtNode) {
        Tok asgmtTok = asgnmtNode.getTok();
        ASTNode lhsNode = asgnmtNode.getLeft();
        ASTNode exprNode = asgnmtNode.getRight();
        TypeInfo lhsDtype = lhsNode.getDtype();
        TypeInfo rhsDtype = exprNode.getDtype();

        // Compare and check if the lhs and rhs have compatible types
        if (rhsDtype == null) {
            return ParseErr.raise(new ErrMsg("No type detected on the right-hand side", asgmtTok));
        } else if (lhsDtype != rhsDtype) {
            if (lhsDtype != null) {
                OpCompat opCompat = new BinOpCompat(TokType.ASSIGNMENT, lhsDtype, rhsDtype);
                if (context.getOpTable().getCompatDtype(opCompat) == null) {
                    return ParseErr.raise(new ErrMsg("Unable to assign data of type '" + rhsDtype.id() +
                            "' to data of type '" + lhsDtype.id() + "'", asgmtTok));
                }
            }
            lhsNode.setDtype(rhsDtype);
            lhsSymbol.setDtype(rhsDtype);
        }

        asgnmtNode.setDtype(rhsDtype);
        return ParseResult.ok(asgnmtNode);
    }
}
