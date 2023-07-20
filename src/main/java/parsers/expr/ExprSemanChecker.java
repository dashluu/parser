package parsers.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import operators.UnOpCompat;
import parsers.utils.ParseContext;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import symbols.FunInfo;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import toks.Tok;
import toks.TokType;
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
            default -> result = checkFunCall((FunCallASTNode) exprNode);
        }

        return result;
    }

    /**
     * Assigns a data type to a literal.
     *
     * @param literalNode an AST node associated with the literal.
     * @return a ParseResult object as the result of assigning a data type to a literal.
     */
    private ParseResult<ASTNode> typeCheckLiteral(LiteralASTNode literalNode) {
        TokType literalTokType = literalNode.getTok().getType();
        TypeInfo dtype = context.getTypeTable().getType(literalTokType);
        literalNode.setDtype(dtype);
        return ParseResult.ok(literalNode);
    }

    /**
     * Checks if an identifier corresponds to a type or is defined.
     *
     * @param idNode an AST node associated with an identifier.
     * @return a ParseResult object as the result of checking the identifier.
     */
    private ParseResult<ASTNode> checkId(ASTNode idNode) {
        Tok idTok = idNode.getTok();
        String id = idTok.getVal();

        // Check if the id corresponds to a data type
        TypeInfo dtype = context.getTypeTable().getType(id);
        if (dtype != null) {
            idNode.setNodeType(ASTNodeType.DTYPE);
            idNode.setDtype(dtype);
            return ParseResult.ok(idNode);
        }

        // Check if the id is valid
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        SymbolInfo symbol = symbolTable.getClosureSymbol(id);
        if (symbol == null) {
            return ParseErr.raise(new ErrMsg("Invalid identifier '" + id + "'", idTok));
        }

        switch (symbol.getSymbolType()) {
            case VAR -> idNode.setNodeType(ASTNodeType.VAR_ID);
            case CONST -> idNode.setNodeType(ASTNodeType.CONST_ID);
            default -> idNode.setNodeType(ASTNodeType.PARAM);
        }

        idNode.setDtype(symbol.getDtype());
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

        // Get the operand's data type
        TypeInfo operandDtype = childNode.getDtype();

        // Check the result's data type after applying the operator
        OpCompat opCompat = new UnOpCompat(opId, operandDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return ParseErr.raise(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
                    operandDtype.id() + "'", opTok));
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
        ASTNode leftNode = exprNode.getLeft();
        ASTNode rightNode = exprNode.getRight();

        // Recursively analyze the semantics of the left and right node
        ParseResult<ASTNode> result = recurCheckSeman(leftNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        result = recurCheckSeman(rightNode);
        if (result.getStatus() == ParseStatus.ERR) {
            return result;
        }

        // Check assignment if there is any
        ASTNodeType leftNodeType = leftNode.getNodeType();
        if (opId == TokType.ASSIGNMENT && leftNodeType != ASTNodeType.VAR_ID && leftNodeType != ASTNodeType.PARAM) {
            return ParseErr.raise(new ErrMsg("Assignments are only valid for mutable identifiers", opTok));
        }

        // Get the left and right node's data type
        TypeInfo leftDtype = leftNode.getDtype();
        TypeInfo rightDtype = rightNode.getDtype();

        // Check the result's data type after applying the operator
        OpCompat opCompat = new BinOpCompat(opId, leftDtype, rightDtype);
        TypeInfo resultDtype = context.getOpTable().getCompatDtype(opCompat);
        if (resultDtype == null) {
            return ParseErr.raise(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
                    leftDtype.id() + "' and type '" + rightDtype.id() + "'", opTok));
        }

        // Set the current node's data type to that of the result
        exprNode.setDtype(resultDtype);
        return ParseResult.ok(exprNode);
    }

    /**
     * Checks if a function identifier exists and if the data type of each expression in the argument list is as expected.
     *
     * @param funCallNode an AST node associated with the function call.
     * @return a ParseResult object as the result of type checking the function call and its argument list.
     */
    private ParseResult<ASTNode> checkFunCall(FunCallASTNode funCallNode) throws IOException {
        Tok funIdTok = funCallNode.getTok();
        String funId = funIdTok.getVal();
        // Check if the function id exists
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        FunInfo funInfo = (FunInfo) symbolTable.getClosureSymbol(funId);
        if (funInfo == null) {
            return ParseErr.raise(new ErrMsg("Invalid function identifier '" + funId + "'", funIdTok));
        }

        Iterator<TypeInfo> paramDtypesIter = funInfo.iterator();
        TypeInfo paramDtype;
        ParseResult<ASTNode> argResult;
        int i = 0;

        // Check if each argument type is as expected
        for (ASTNode argNode : funCallNode) {
            paramDtype = paramDtypesIter.next();
            argResult = recurCheckSeman(argNode);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            } else if (!argNode.getDtype().equals(paramDtype)) {
                return ParseErr.raise(new ErrMsg("Expected type '" + paramDtype.id() + "' for argument " + i,
                        argNode.getTok()));
            }
            ++i;
        }

        int numArgs = funInfo.countParams();
        if (i != numArgs) {
            return ParseErr.raise(new ErrMsg("Expected the number of arguments to be " + numArgs + " but got " + i +
                    " for function '" + funId + "'", funIdTok));
        }

        funCallNode.setDtype(funInfo.getDtype());
        return ParseResult.ok(funCallNode);
    }
}
