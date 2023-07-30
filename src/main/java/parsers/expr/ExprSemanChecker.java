package parsers.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import operators.UnOpCompat;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import symbols.*;
import toks.Tok;
import toks.TokType;
import types.ArrTypeInfo;
import types.IntType;
import types.TypeInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

public class ExprSemanChecker {
    private ParseContext context;

    /**
     * Checks the semantics of an expression.
     *
     * @param exprNode the expression AST's root.
     * @param context  the parsing context.
     * @return a ParseResult object as the result of checking the expression's semantics.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode exprNode, ParseContext context) throws IOException {
        this.context = context;
        return recurCheckSeman(exprNode);
    }

    /**
     * Recursively checks the semantics of an expression.
     *
     * @param exprNode the expression AST's root.
     * @return a ParseResult object as the result of recursively checking the expression's semantics.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> recurCheckSeman(ASTNode exprNode) throws IOException {
        ParseResult<ASTNode> result;
        ASTNodeType exprNodeType = exprNode.getNodeType();

        switch (exprNodeType) {
            case LITERAL -> result = typeCheckLiteral((LiteralASTNode) exprNode);
            case ID -> result = checkId(exprNode);
            case UN_OP -> result = typeCheckUnExpr((UnOpASTNode) exprNode);
            case BIN_OP -> result = typeCheckBinExpr((BinOpASTNode) exprNode);
            case ARR_ACCESS -> result = checkArrAccess((ArrAccessASTNode) exprNode);
            default -> result = checkFunCall((FunCallASTNode) exprNode);
        }

        return result;
    }

    /**
     * Assigns a data type to a literal.
     *
     * @param literalNode the AST node associated with the literal.
     * @return a ParseResult object as the result of assigning a data type to a literal.
     */
    private ParseResult<ASTNode> typeCheckLiteral(LiteralASTNode literalNode) {
        TokType literalTokType = literalNode.getTok().getType();
        TypeInfo dtype = context.getTypeTable().getType(literalTokType);
        literalNode.setDtype(dtype);
        return ParseResult.ok(literalNode);
    }

    /**
     * Checks if an identifier corresponds to a type or a variable.
     *
     * @param idNode the AST node associated with the identifier.
     * @return a ParseResult object as the result of checking the identifier.
     */
    private ParseResult<ASTNode> checkId(ASTNode idNode) {
        Tok idTok = idNode.getTok();
        String id = idTok.getVal();

        // Check if the id corresponds to a data type
        TypeInfo dtype = context.getTypeTable().getType(id);
        if (dtype != null) {
            idNode = new DtypeASTNode(idTok, dtype);
            return ParseResult.ok(idNode);
        }

        // Check if the id is valid
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(id);
        if (symbol == null || (symbol.getSymbolType() != SymbolType.VAR &&
                symbol.getSymbolType() != SymbolType.PARAM)) {
            return context.raiseErr(new ErrMsg("Invalid identifier '" + id + "'", idTok));
        }

        dtype = symbol.getDtype();
        boolean mutable = symbol.isMutable();
        idNode = new IdASTNode(idTok, dtype, mutable);
        return ParseResult.ok(idNode);
    }

    /**
     * Checks the type compatibilities in a unary expression.
     *
     * @param exprNode the unary expression AST's root.
     * @return a ParseResult object as the result of type checking a unary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> typeCheckUnExpr(UnOpASTNode exprNode) throws IOException {
        Tok opTok = exprNode.getTok();
        TokType opId = opTok.getType();
        ASTNode childNode = exprNode.getChild();

        // Recursively analyze the semantics of the child node
        ParseResult<ASTNode> result = recurCheckSeman(childNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        // Update the child node
        childNode = result.getData();
        exprNode.setChild(childNode);
        // Get the operand's data type
        TypeInfo operandDtype = childNode.getDtype();

        // Check the result's data type after applying the operator
        OpCompat opCompat = new UnOpCompat(opId, operandDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return context.raiseErr(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
                    operandDtype.getId() + "'", opTok));
        }

        // Set the current node's data type to that of the result
        exprNode.setDtype(resultDtype);
        return ParseResult.ok(exprNode);
    }

    /**
     * Checks the type compatibilities in a binary expression.
     *
     * @param exprNode the binary expression AST's root.
     * @return a ParseResult object as the result of type checking a binary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> typeCheckBinExpr(BinOpASTNode exprNode) throws IOException {
        Tok opTok = exprNode.getTok();
        TokType opId = opTok.getType();
        // Recursively analyze the semantics of the left child
        ASTNode leftNode = exprNode.getLeft();
        ParseResult<ASTNode> result = recurCheckSeman(leftNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        //Update the left child
        leftNode = result.getData();
        exprNode.setLeft(leftNode);
        // Recursively analyze the semantics of the right node
        ASTNode rightNode = exprNode.getRight();
        result = recurCheckSeman(rightNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        // Update the right child
        rightNode = result.getData();
        exprNode.setRight(rightNode);

        // TODO: Check assignment if there is any
        if (opId == TokType.ASSIGNMENT) {
        }

        // Get the left and right node's data type
        TypeInfo leftDtype = leftNode.getDtype();
        TypeInfo rightDtype = rightNode.getDtype();

        // Check the result's data type after applying the operator
        OpCompat opCompat = new BinOpCompat(opId, leftDtype, rightDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return context.raiseErr(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
                    leftDtype.getId() + "' and type '" + rightDtype.getId() + "'", opTok));
        }

        // Set the current node's data type to that of the result
        exprNode.setDtype(resultDtype);
        return ParseResult.ok(exprNode);
    }

    /**
     * Checks if an array access expression is valid.
     *
     * @param arrAccessNode the AST node associated with the array access expression.
     * @return a ParseResult object as the result of checking the array access expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> checkArrAccess(ArrAccessASTNode arrAccessNode) throws IOException {
        Tok arrIdTok = arrAccessNode.getTok();
        String arrId = arrIdTok.getVal();
        // Check if the array id exists
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(arrId);
        if (symbol == null || (symbol.getSymbolType() != SymbolType.VAR &&
                symbol.getSymbolType() != SymbolType.PARAM)) {
            return context.raiseErr(new ErrMsg("Invalid array identifier '" + arrId + "'", arrIdTok));
        }

        // Check if the id is of type array
        TypeInfo dtype = symbol.getDtype();
        if (!dtype.getId().equals(ArrTypeInfo.ID)) {
            return context.raiseErr(new ErrMsg("Expected an array type from '" + arrId + "'", arrIdTok));
        }

        ParseResult<ASTNode> itemResult;
        int i = 0;

        // Check if each index is of type integer
        for (ASTNode itemNode : arrAccessNode) {
            itemResult = recurCheckSeman(itemNode);
            if (itemResult.getStatus() == ParseStatus.ERR) {
                return itemResult;
            } else if (!itemNode.getDtype().equals(IntType.getInst())) {
                return context.raiseErr(new ErrMsg("Expected type '" + IntType.ID + "' for array index",
                        itemNode.getTok()));
            }
            ++i;
        }

        ArrTypeInfo arrDtype = (ArrTypeInfo) dtype;
        int dim = arrDtype.getDim();
        // Reduce the dimension of the original array data type
        ArrTypeInfo subarrDtype = new ArrTypeInfo(dim - i);
        arrAccessNode.setDtype(subarrDtype);
        arrAccessNode.setMutable(symbol.isMutable());
        return ParseResult.ok(arrAccessNode);
    }

    /**
     * Checks if a function call is valid.
     *
     * @param funCallNode the AST node associated with the function call.
     * @return a ParseResult object as the result of checking the function call.
     */
    private ParseResult<ASTNode> checkFunCall(FunCallASTNode funCallNode) throws IOException {
        Tok funIdTok = funCallNode.getTok();
        String funId = funIdTok.getVal();
        // Check if the function id exists
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(funId);
        if (symbol == null || symbol.getSymbolType() != SymbolType.FUN) {
            return context.raiseErr(new ErrMsg("Invalid function identifier '" + funId + "'", funIdTok));
        }

        FunInfo funInfo = (FunInfo) symbol;
        Iterator<TypeInfo> paramDtypesIter = funInfo.iterator();
        TypeInfo paramDtype;
        ParseResult<ASTNode> argResult;
        ASTNode argNode;
        ListIterator<ASTNode> argIter = funCallNode.listIterator();
        int i = 0;

        while (argIter.hasNext()) {
            argNode = argIter.next();
            // Check if each argument type is as expected
            paramDtype = paramDtypesIter.next();
            argResult = recurCheckSeman(argNode);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            } else if (!argNode.getDtype().equals(paramDtype)) {
                return context.raiseErr(new ErrMsg("Expected type '" + paramDtype.getId() + "' for argument " + i,
                        argNode.getTok()));
            }
            // Update the AST child node at the current position
            argIter.set(argResult.getData());
            ++i;
        }

        int numArgs = funInfo.countParams();
        if (i != numArgs) {
            return context.raiseErr(new ErrMsg("Expected the number of arguments to be " + numArgs + " but got " +
                    i + " for function '" + funId + "'", funIdTok));
        }

        funCallNode.setDtype(funInfo.getDtype());
        return ParseResult.ok(funCallNode);
    }
}
