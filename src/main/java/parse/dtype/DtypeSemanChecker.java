package parse.dtype;

import ast.ASTNodeType;
import ast.ArrDtypeASTNode;
import ast.DtypeASTNode;
import ast.SimpleDtypeASTNode;
import exceptions.ErrMsg;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import toks.Tok;
import types.TypeInfo;

public class DtypeSemanChecker {

    /**
     * Checks if a data type is valid.
     *
     * @param dtypeNode the AST node associated with a data type.
     * @param context   the parsing context.
     * @return a ParseResult object as the result of checking the data type.
     */
    public ParseResult<TypeInfo> checkDtype(DtypeASTNode dtypeNode, ParseContext context) {
        if (dtypeNode.getNodeType() == ASTNodeType.SIMPLE_DTYPE) {
            return checkSimpleDtype((SimpleDtypeASTNode) dtypeNode, context);
        }
        return checkArrDtype((ArrDtypeASTNode) dtypeNode, context);
    }

    /**
     * Checks if a simple(non-array) data type is valid.
     *
     * @param simpleDtypeNode the AST node associated with a simple data type.
     * @param context         the parsing context.
     * @return a ParseResult object as the result of checking the simple data type.
     */
    private ParseResult<TypeInfo> checkSimpleDtype(SimpleDtypeASTNode simpleDtypeNode, ParseContext context) {
        Tok dtypeTok = simpleDtypeNode.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = context.getTypeTable().getType(dtypeId);
        if (dtype == null) {
            return context.raiseErr(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }
        return ParseResult.ok(dtype);
    }

    /**
     * Checks if an array data type is valid.
     *
     * @param arrDtypeNode the AST node associated with an array data type.
     * @param context      the parsing context.
     * @return a ParseResult object as the result of checking the array data type.
     */
    private ParseResult<TypeInfo> checkArrDtype(ArrDtypeASTNode arrDtypeNode, ParseContext context) {
        DtypeASTNode elmDtypeNode = arrDtypeNode.getElmDtypeNode();
        if (elmDtypeNode.getNodeType() != ASTNodeType.ARR_DTYPE) {
            return checkSimpleDtype((SimpleDtypeASTNode) elmDtypeNode, context);
        }
        return checkArrDtype((ArrDtypeASTNode) elmDtypeNode, context);
    }
}
