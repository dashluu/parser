package parse.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import operators.UnOpCompat;
import parse.dtype.DtypeSemanChecker;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import symbols.FunInfo;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.SymbolType;
import toks.Tok;
import toks.TokType;
import types.ArrType;
import types.IntType;
import types.TypeInfo;

import java.io.IOException;
import java.util.Iterator;

public class ExprSemanChecker {
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
            case ARR_LITERAL -> result = checkArrLiteral((ArrLiteralASTNode) exprNode);
            case FUN_CALL -> result = checkFunCall((FunCallASTNode) exprNode);
            default -> result = dtypeSemanChecker.checkDtype((DtypeASTNode) exprNode, context);
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
        ParseResult<ASTNode> result = recurCheckSeman(exprNode);
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

        // Update the expression node
        unOpNode.setExprNode(exprNode);
        // Set the current node's data type to that of the result
        unOpNode.setDtype(resultDtype);
        return ParseResult.ok(unOpNode);
    }

    /**
     * Checks if the left-hand side of the assignment node is holding an lvalue.
     *
     * @param assignmentNode the assignment node.
     * @return a ParseResult object as the result of checking the left-hand side of the assignment node.
     */
    private ParseResult<ASTNode> checkAssignmentLvalue(BinOpASTNode assignmentNode) {
        ASTNode leftNode = assignmentNode.getLeft();
        ASTNodeType leftNodeType = leftNode.getNodeType();
        if ((leftNodeType != ASTNodeType.ID && leftNodeType != ASTNodeType.ARR_ACCESS) ||
                (leftNodeType == ASTNodeType.ARR_ACCESS && leftNode.getDtype().getId().equals(ArrType.ID))) {
            return context.raiseErr(new ErrMsg("The left-hand side of '" + assignmentNode.getTok().getVal() +
                    "' is not an lvalue", leftNode.getSrcRange()));
        }

        IdASTNode idNode;
        if (leftNodeType == ASTNodeType.ID) {
            idNode = (IdASTNode) leftNode;
        } else {
            ArrAccessASTNode leftArrAccessNode = (ArrAccessASTNode) leftNode;
            idNode = leftArrAccessNode.getIdNode();
        }

        Tok idTok = idNode.getTok();
        if (!idNode.isMutable()) {
            return context.raiseErr(new ErrMsg("Identifier '" + idTok.getVal() + "' is not mutable", idTok));
        }

        return ParseResult.ok(assignmentNode);
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
        binOpNode.setLeft(leftNode);
        TypeInfo leftDtype = leftNode.getDtype();
        // Recursively analyze the semantics of the right node
        ASTNode rightNode = binOpNode.getRight();
        result = recurCheckSeman(rightNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        rightNode = result.getData();
        binOpNode.setRight(rightNode);
        TypeInfo rightDtype = rightNode.getDtype();

        if (opId == TokType.ASSIGNMENT) {
            // Check assignment operator
            result = checkAssignmentLvalue(binOpNode);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            }
        } else if (opId == TokType.TYPE_CONV) {
            // Check type conversion operator
            if (rightNode.getNodeType() != ASTNodeType.SIMPLE_DTYPE) {
                return context.raiseErr(new ErrMsg("Expected a non-array data type on the right-hand side of '" +
                        opVal + "' operator", rightNode.getSrcRange()));
            }
        }

        // Check the result's data type after applying the operator
        OpCompat opCompat = new BinOpCompat(opId, leftDtype, rightDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return context.raiseErr(new ErrMsg("Operator '" + opVal + "' is not compatible with type '" +
                    leftDtype.getId() + "' and type '" + rightDtype.getId() + "'", opTok));
        }

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
        TypeInfo dtype = symbol.getDtype();
        if (dtype.isPrimitive()) {
            return context.raiseErr(new ErrMsg("Invalid array identifier '" + arrId + "'", arrIdTok));
        }

        ParseResult<ASTNode> indexResult;
        ASTNode indexNode;
        ExprListASTNode indexListNode = arrAccessNode.getIndexListNode();
        IASTNodeIterator indexIter = indexListNode.nodeIterator();
        int i = 0;

        while (indexIter.hasNext()) {
            indexNode = indexIter.next();
            indexResult = recurCheckSeman(indexNode);
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
            ++i;
        }

        ArrType arrDtype = (ArrType) dtype;
        if (i > arrDtype.getNumDims()) {
            return context.raiseErr(new ErrMsg("Accessing array '" + arrId + "' requires <= " +
                    arrDtype.getNumDims() + " dimensions but got " + i, arrAccessNode.getSrcRange()));
        }

        arrIdNode.setDtype(arrDtype);
        arrIdNode.setMutable(symbol.isMutable());
        TypeInfo arrAccessDtype = arrDtype.getNestedElmDtype(i);
        arrAccessNode.setDtype(arrAccessDtype);
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
        TypeInfo arrElmDtype, elmDtype;
        ArrType arrDtype;
        int elmArrNumElms = 0;
        IASTNodeIterator elmIter = arrLiteralNode.nodeIterator();
        boolean firstElm = true;

        while (elmIter.hasNext()) {
            elmNode = elmIter.next();
            elmResult = recurCheckSeman(elmNode);
            if (elmResult.getStatus() == ParseStatus.ERR) {
                return elmResult;
            }

            elmNode = elmResult.getData();
            elmDtype = elmNode.getDtype();
            if (elmNode.getNodeType() != ASTNodeType.ARR_LITERAL && elmDtype.getId().equals(ArrType.ID)) {
                // Element as an array reference but not an array literal
                return context.raiseErr(new ErrMsg("Cannot have an array reference element inside an array literal",
                        elmNode.getSrcRange()));
            }

            if (firstElm) {
                firstElm = false;
                arrDtype = new ArrType(elmDtype);
                arrLiteralNode.setDtype(arrDtype);
                if (elmNode.getNodeType() == ASTNodeType.ARR_LITERAL) {
                    elmArrNumElms = ((ArrLiteralASTNode) elmNode).countChildren();
                }
            } else {
                // Check if the array is homogeneous
                arrDtype = (ArrType) arrLiteralNode.getDtype();
                arrElmDtype = arrDtype.getElmDtype();
                if (!arrElmDtype.equals(elmDtype) || (arrElmDtype.getId().equals(ArrType.ID) &&
                        ((ArrLiteralASTNode) elmNode).countChildren() != elmArrNumElms)) {
                    return context.raiseErr(new ErrMsg("An array must be homogeneous", elmNode.getSrcRange()));
                }
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
            argResult = recurCheckSeman(argNode);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            }

            argNode = argResult.getData();
            if (!argNode.getDtype().equals(paramDtype)) {
                return context.raiseErr(new ErrMsg("Expected type '" + paramDtype.getId() + "' for argument " +
                        (i + 1), argNode.getSrcRange()));
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
