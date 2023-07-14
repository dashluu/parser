package parsers.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.BinOpCompat;
import operators.OpCompat;
import operators.OpTable;
import operators.UnOpCompat;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.Scope;
import symbols.FunInfo;
import symbols.SymbolTable;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;

import java.io.IOException;
import java.util.Iterator;

public class ExprSemanChecker {
    private Scope scope;
    private static final OpTable opTable = OpTable.getInst();
    private static final ParseErr err = ParseErr.getInst();

    /**
     * Checks the semantics of an expression.
     *
     * @param exprNode the expression AST's root.
     * @param scope    the scope surrounding the expression.
     * @return a ParseResult object as the result of checking the expression's semantics.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> checkSeman(ASTNode exprNode, Scope scope) throws IOException {
        this.scope = scope;
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
        ASTNodeType exprNodeType = exprNode.getNodeType();
        if (exprNodeType == ASTNodeType.LITERAL || exprNodeType == ASTNodeType.VAR_ID ||
                exprNodeType == ASTNodeType.CONST_ID || exprNodeType == ASTNodeType.PARAM ||
                exprNodeType == ASTNodeType.DTYPE) {
            return ParseResult.ok(exprNode);
        }

        ParseResult<ASTNode> result;

        if (exprNode.getNodeType() == ASTNodeType.UN_OP) {
            result = typeCheckUnExpr((UnASTNode) exprNode);
        } else if (exprNode.getNodeType() == ASTNodeType.BIN_OP) {
            result = typeCheckBinExpr((BinASTNode) exprNode);
        } else {
            result = typeCheckArgList((KnaryASTNode) exprNode);
        }

        return result;
    }

    /**
     * Checks the type compatibilities in a unary expression.
     *
     * @param exprNode the unary expression AST's root.
     * @return a ParseResult object as the result of type checking a unary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> typeCheckUnExpr(UnASTNode exprNode) throws IOException {
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
        TypeInfo resultDtype = opTable.getCompatDtype(opCompat);
        if (resultDtype == null) {
            return err.raise(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
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
    private ParseResult<ASTNode> typeCheckBinExpr(BinASTNode exprNode) throws IOException {
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

        // Get the left and right node's data type
        TypeInfo leftDtype = leftNode.getDtype();
        TypeInfo rightDtype = rightNode.getDtype();

        // Check the result's data type after applying the operator
        OpCompat opCompat = new BinOpCompat(opId, leftDtype, rightDtype);
        TypeInfo resultDtype = opTable.getCompatDtype(opCompat);
        if (resultDtype == null) {
            return err.raise(new ErrMsg("Operator '" + opTok.getVal() + "' is not compatible with type '" +
                    leftDtype.id() + "' and type '" + rightDtype.id() + "'", opTok));
        }

        // Set the current node's data type to that of the result
        exprNode.setDtype(resultDtype);
        return ParseResult.ok(exprNode);
    }

    /**
     * Checks the data type of each expression in the argument list is as expected.
     *
     * @param funCallNode an AST node associated with the function call.
     * @return a ParseResult object as the result of type checking the function's argument list.
     */
    private ParseResult<ASTNode> typeCheckArgList(KnaryASTNode funCallNode) throws IOException {
        Tok funCallTok = funCallNode.getTok();
        String funId = funCallTok.getVal();
        SymbolTable symbolTable = scope.getSymbolTable();
        FunInfo funInfo = (FunInfo) symbolTable.getSymbol(funId);
        Iterator<TypeInfo> paramDtypesIter = funInfo.iterator();
        TypeInfo paramDtype;
        ParseResult<ASTNode> argResult;
        int i = 1;

        // Check if each argument type is as expected
        for (ASTNode child : funCallNode) {
            paramDtype = paramDtypesIter.next();
            argResult = recurCheckSeman(child);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            } else if (!child.getDtype().equals(paramDtype)) {
                return err.raise(new ErrMsg("Expected type '" + paramDtype.id() + "' for argument " + i,
                        child.getTok()));
            }
            ++i;
        }

        return ParseResult.ok(funCallNode);
    }
}
