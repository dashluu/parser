package parse.dtype;

import ast.*;
import exceptions.ErrMsg;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import toks.Tok;
import types.ArrType;
import types.TypeInfo;

public class DtypeSemanChecker {

    /**
     * Checks if a data type is valid.
     *
     * @param dtypeNode the AST node associated with a data type.
     * @param context   the parsing context.
     * @return a ParseResult object as the result of checking the data type.
     */
    public ParseResult<ASTNode> checkDtype(DtypeASTNode dtypeNode, ParseContext context) {
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
    private ParseResult<ASTNode> checkSimpleDtype(SimpleDtypeASTNode simpleDtypeNode, ParseContext context) {
        Tok dtypeTok = simpleDtypeNode.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = context.getTypeTable().getType(dtypeId);
        if (dtype == null) {
            return context.raiseErr(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }

        simpleDtypeNode.setDtype(dtype);
        return ParseResult.ok(simpleDtypeNode);
    }

    /**
     * Checks if an array data type is valid.
     *
     * @param arrDtypeNode the AST node associated with an array data type.
     * @param context      the parsing context.
     * @return a ParseResult object as the result of checking the array data type.
     */
    private ParseResult<ASTNode> checkArrDtype(ArrDtypeASTNode arrDtypeNode, ParseContext context) {
        DtypeASTNode elmDtypeNode = arrDtypeNode.getElmDtypeNode();
        ParseResult<ASTNode> elmDtypeResult;

        if (elmDtypeNode.getNodeType() == ASTNodeType.SIMPLE_DTYPE) {
            // The element's data type is a non-array data type
            elmDtypeResult = checkSimpleDtype((SimpleDtypeASTNode) elmDtypeNode, context);
        } else {
            // The element's data type is an array data type
            elmDtypeResult = checkArrDtype((ArrDtypeASTNode) elmDtypeNode, context);
        }

        if (elmDtypeResult.getStatus() == ParseStatus.ERR) {
            return elmDtypeResult;
        }

        ArrType arrDtype = (ArrType) arrDtypeNode.getDtype();
        arrDtype.setElmDtype(elmDtypeNode.getDtype());
        return ParseResult.ok(arrDtypeNode);
    }
}
