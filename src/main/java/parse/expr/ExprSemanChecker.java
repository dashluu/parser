package parse.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import operators.UnOpCompat;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import symbols.FunInfo;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.SymbolType;
import toks.Tok;
import toks.TokType;
import types.ArrTypeInfo;
import types.IntType;
import types.TypeInfo;

import java.io.IOException;
import java.util.Iterator;

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
        return checkValExprSeman(exprNode);
    }

    /**
     * Checks if an expression is a value expression, that is, it carries a value and not just a data type.
     *
     * @param exprNode the expression AST's root to be checked.
     * @return a ParseResult object as the result of checking the expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> checkValExprSeman(ASTNode exprNode) throws IOException {
        ParseResult<ASTNode> result = recurCheckSeman(exprNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        exprNode = result.getData();
        if (!exprNode.isValExpr()) {
            return context.raiseErr(new ErrMsg("Expected a value expression", exprNode.getSrcRange()));
        }

        return result;
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
            case ARR_LITERAL -> result = checkArrLiteral((ArrLiteralASTNode) exprNode);
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
        TokType literalTokType = literalNode.getTok().getTokType();
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
            idNode = new SimpleDtypeASTNode(idTok, dtype);
            return ParseResult.ok(idNode);
        }

        // Check if the id is valid
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(id);
        if (symbol == null || (symbol.getSymbolType() != SymbolType.VAR && symbol.getSymbolType() != SymbolType.PARAM)) {
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
     * @param unOpNode the unary expression AST's root.
     * @return a ParseResult object as the result of type checking a unary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> typeCheckUnExpr(UnOpASTNode unOpNode) throws IOException {
        Tok opTok = unOpNode.getTok();
        String opVal = opTok.getVal();
        TokType opId = opTok.getTokType();
        ASTNode exprNode = unOpNode.getExprNode();

        // Recursively analyze the semantics of the operand node
        ParseResult<ASTNode> result = checkValExprSeman(exprNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        exprNode = result.getData();
        // Check the result's data type after applying the operator
        TypeInfo operandDtype = exprNode.getDtype();
        OpCompat opCompat = new UnOpCompat(opId, operandDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return context.raiseErr(new ErrMsg("Operator '" + opVal + "' is not compatible with data type '" +
                    operandDtype.getId() + "'", opTok));
        }

        // Update the child node
        unOpNode.setExprNode(exprNode);
        // Set the current node's data type to that of the result
        unOpNode.setDtype(resultDtype);
        return ParseResult.ok(unOpNode);
    }

    /**
     * Checks if the left-hand side node of an operator is holding an lvalue.
     *
     * @param leftNode the left-hand side node of the operator.
     * @param opTok    the operator token.
     * @return a ParseResult object as the result of checking the left-hand side node of the operator.
     */
    private ParseResult<ASTNode> checkLvalue(ASTNode leftNode, Tok opTok) {
        IdASTNode leftIdNode;
        ASTNodeType leftNodeType = leftNode.getNodeType();

        if (leftNodeType == ASTNodeType.ID) {
            leftIdNode = (IdASTNode) leftNode;
        } else if (leftNodeType == ASTNodeType.ARR_ACCESS) {
            ArrAccessASTNode leftArrAccessNode = (ArrAccessASTNode) leftNode;
            leftIdNode = leftArrAccessNode.getIdNode();
        } else {
            return context.raiseErr(new ErrMsg("The left-hand side of '" + opTok.getVal() + "' is not lvalue",
                    leftNode.getSrcRange()));
        }

        Tok leftTok = leftIdNode.getTok();
        if (!leftIdNode.isMutable()) {
            return context.raiseErr(new ErrMsg("Identifier '" + leftTok.getVal() + "' is not mutable", leftTok));
        }

        return ParseResult.ok(leftNode);
    }

    /**
     * Checks the type compatibilities in a binary expression.
     *
     * @param binOpNode the binary expression AST's root.
     * @return a ParseResult object as the result of type checking a binary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> typeCheckBinExpr(BinOpASTNode binOpNode) throws IOException {
        Tok opTok = binOpNode.getTok();
        String opVal = opTok.getVal();
        TokType opId = opTok.getTokType();
        // Recursively analyze the semantics of the left child
        ASTNode leftNode = binOpNode.getLeft();
        ParseResult<ASTNode> result = recurCheckSeman(leftNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        leftNode = result.getData();
        TypeInfo leftDtype = leftNode.getDtype();
        // Recursively analyze the semantics of the right node
        ASTNode rightNode = binOpNode.getRight();
        result = recurCheckSeman(rightNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        rightNode = result.getData();
        TypeInfo rightDtype = rightNode.getDtype();

        if (opId == TokType.ASSIGNMENT) {
            // Check assignment operator
            result = checkLvalue(leftNode, opTok);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            }
            leftNode = result.getData();
        } else if (opId == TokType.TYPE_CONV) {
            // Check type conversion operator
            if (rightNode.getNodeType() != ASTNodeType.SIMPLE_DTYPE) {
                return context.raiseErr(new ErrMsg("Expected a data type on the right-hand side of '" + opVal +
                        "' operator", rightNode.getSrcRange()));
            }
        } else if (!leftNode.isValExpr() || !rightNode.isValExpr()) {
            return context.raiseErr(new ErrMsg("Each operand on both sides of '" + opVal +
                    "' must be a value expression", opTok));
        }

        // Check the result's data type after applying the operator
        OpCompat opCompat = new BinOpCompat(opId, leftDtype, rightDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return context.raiseErr(new ErrMsg("Operator '" + opVal + "' is not compatible with type '" +
                    leftDtype.getId() + "' and type '" + rightDtype.getId() + "'", opTok));
        }

        // Update the left and right child
        binOpNode.setLeft(leftNode);
        binOpNode.setRight(rightNode);
        // Set the current node's data type to that of the result
        binOpNode.setDtype(resultDtype);
        return ParseResult.ok(binOpNode);
    }

    /**
     * Checks if an array access expression is valid.
     *
     * @param arrAccessNode the AST node associated with the array access expression.
     * @return a ParseResult object as the result of checking the array access expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> checkArrAccess(ArrAccessASTNode arrAccessNode) throws IOException {
        IdASTNode arrIdNode = arrAccessNode.getIdNode();
        Tok arrIdTok = arrIdNode.getTok();
        String arrId = arrIdTok.getVal();
        // Check if the array id exists
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(arrId);
        if (symbol == null || (symbol.getSymbolType() != SymbolType.VAR && symbol.getSymbolType() != SymbolType.PARAM)) {
            return context.raiseErr(new ErrMsg("Invalid array identifier '" + arrId + "'", arrIdTok));
        }

        // Check if the id is of type array
        TypeInfo dtype = arrIdNode.getDtype();
        if (dtype.isPrimitive()) {
            return context.raiseErr(new ErrMsg("Expected an array identifier '" + arrId + "'", arrIdTok));
        }

        ParseResult<ASTNode> indexResult;
        ASTNode indexNode;
        ExprListASTNode indexListNode = arrAccessNode.getIndexListNode();
        IASTNodeIterator indexIter = indexListNode.nodeIterator();

        while (indexIter.hasNext()) {
            indexNode = indexIter.next();
            indexResult = checkValExprSeman(indexNode);
            if (indexResult.getStatus() == ParseStatus.ERR) {
                return indexResult;
            }

            // Check if each index is of type integer
            indexNode = indexResult.getData();
            if (!indexNode.getDtype().equals(IntType.getInst())) {
                return context.raiseErr(new ErrMsg("Expected data type '" + IntType.ID + "' for array index",
                        indexNode.getSrcRange()));
            }

            // Update the index node at the current position
            indexIter.set(indexNode);
        }

        ArrTypeInfo arrDtype = (ArrTypeInfo) dtype;
        TypeInfo coreDtype = arrDtype.getCoreDtype();
        ArrTypeInfo newArrDtype = new ArrTypeInfo(coreDtype);
        arrAccessNode.setDtype(newArrDtype);
        return ParseResult.ok(arrAccessNode);
    }

    /**
     * Checks if an array literal is valid.
     *
     * @param arrLiteralNode the AST node associated with the array literal.
     * @return a ParseResult object as the result of checking the array literal.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> checkArrLiteral(ArrLiteralASTNode arrLiteralNode) throws IOException {
        ParseResult<ASTNode> elmResult;
        ASTNode elmNode;
        TypeInfo elmDtype, arrCoreDtype, elmCoreDtype, resultDtype;
        ArrTypeInfo arrDtype, elmArrDtype;
        IASTNodeIterator elmIter = arrLiteralNode.nodeIterator();
        boolean firstElm = true;

        while (elmIter.hasNext()) {
            elmNode = elmIter.next();
            elmResult = checkValExprSeman(elmNode);
            if (elmResult.getStatus() == ParseStatus.ERR) {
                return elmResult;
            }

            // Check if the current element node's data type is compatible with that of the array
            elmNode = elmResult.getData();
            arrDtype = (ArrTypeInfo) arrLiteralNode.getDtype();
            // Data type of the element, which can be primitive or array
            elmDtype = elmNode.getDtype();

            if (firstElm) {
                firstElm = false;
                if (elmDtype.isPrimitive()) {
                    // The element is not an array
                    arrDtype.setCoreDtype(elmDtype);
                } else {
                    elmArrDtype = (ArrTypeInfo) elmDtype;
                    arrCoreDtype = elmArrDtype.getCoreDtype();
                    arrDtype.setCoreDtype(arrCoreDtype);
                }
            } else {
                if (elmDtype.isPrimitive()) {
                    elmCoreDtype = elmDtype;
                } else {
                    elmArrDtype = (ArrTypeInfo) elmDtype;
                    elmCoreDtype = elmArrDtype.getCoreDtype();
                }

                // Treat the current node as a type conversion node and check if the data type compatibility
                // between the array and its element
                arrCoreDtype = arrDtype.getCoreDtype();
                OpCompat opCompat = new BinOpCompat(TokType.ARR_TYPE_CONV, arrCoreDtype, elmCoreDtype);
                resultDtype = context.getOpTable().getCompatDtype(opCompat);
                if (resultDtype == null) {
                    return context.raiseErr(new ErrMsg("Unable to have data of type '" + elmCoreDtype.getId() +
                            "' in an array of type '" + arrCoreDtype.getId() + "'", elmNode.getSrcRange()));
                }

                arrDtype.setCoreDtype(resultDtype);
            }

            // Update the element node at the current position
            elmIter.set(elmNode);
        }

        return ParseResult.ok(arrLiteralNode);
    }

    /**
     * Checks if a function call is valid.
     *
     * @param funCallNode the AST node associated with the function call.
     * @return a ParseResult object as the result of checking the function call.
     */
    private ParseResult<ASTNode> checkFunCall(FunCallASTNode funCallNode) throws IOException {
        IdASTNode funIdNode = funCallNode.getIdNode();
        Tok funIdTok = funIdNode.getTok();
        String funId = funIdTok.getVal();
        // Check if the function id exists
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(funId);
        if (symbol == null || symbol.getSymbolType() != SymbolType.FUNCTION) {
            return context.raiseErr(new ErrMsg("Invalid function identifier '" + funId + "'", funIdTok));
        }

        FunInfo funInfo = (FunInfo) symbol;
        Iterator<TypeInfo> paramDtypesIter = funInfo.iterator();
        TypeInfo paramDtype;
        ParseResult<ASTNode> argResult;
        ASTNode argNode;
        MultichildASTNode argListNode = funCallNode.getArgListNode();
        IASTNodeIterator argIter = argListNode.nodeIterator();
        int i = 0;

        while (argIter.hasNext()) {
            argNode = argIter.next();
            // Check if each argument type is as expected
            paramDtype = paramDtypesIter.next();
            argResult = checkValExprSeman(argNode);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            }

            argNode = argResult.getData();
            if (!argNode.getDtype().equals(paramDtype)) {
                return context.raiseErr(new ErrMsg("Expected type '" + paramDtype.getId() + "' for argument " + i,
                        argNode.getSrcRange()));
            }

            // Update the argument node at the current position
            argIter.set(argNode);
            ++i;
        }

        int numArgs = funInfo.countParams();
        if (i != numArgs) {
            return context.raiseErr(new ErrMsg("Expected the number of arguments to be " + numArgs + " but got " +
                    i + " for function '" + funId + "'", argListNode.getSrcRange()));
        }

        funCallNode.setDtype(funInfo.getDtype());
        return ParseResult.ok(funCallNode);
    }
}
