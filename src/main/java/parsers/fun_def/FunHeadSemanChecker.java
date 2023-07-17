package parsers.fun_def;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.ParamListASTNode;
import exceptions.ErrMsg;
import parsers.utils.*;
import symbols.FunInfo;
import symbols.SymbolTable;
import toks.Tok;
import types.TypeInfo;
import types.TypeTable;

public class FunHeadSemanChecker {
    private Scope funScope;
    private static final TypeTable TYPE_TABLE = TypeTable.getInst();

    public ParseResult<ASTNode> checkSeman(FunDefASTNode funDefNode, Scope funScope) {
        this.funScope = funScope;
        // Check function's id
        Tok idTok = funDefNode.getTok();
        ParseResult<FunInfo> idResult = checkId(idTok);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        ParamListASTNode paramListNode = funDefNode.getParamListNode();

    }

    private ParseResult<FunInfo> checkId(Tok idTok) {
        // Check if the function id has been defined
        String id = idTok.getVal();
        SymbolTable symbolTable = funScope.getSymbolTable();
        if (symbolTable.getLocalSymbol(id) != null) {
            return ParseErr.raise(new ErrMsg("'" + id + "' cannot be redeclared", idTok));
        }

        // Create a new function
        FunInfo funInfo = new FunInfo(id, null);
        symbolTable.registerSymbol(funInfo);
        return ParseResult.ok(funInfo);
    }

    private ParseResult<ASTNode> checkParamList(FunInfo funInfo, ParamListASTNode paramListNode) {

    }

    private ParseResult<TypeInfo> checkRetDtype(TypeInfo dummyRetDtype) {
        // Return type's id
        String retDtypeId = dummyRetDtype.id();
        TypeInfo retDtype = TYPE_TABLE.getType(retDtypeId);
        if (retDtype == null) {
            return ParseErr.raise(new ErrMsg("Invalid return type '" + retDtypeId + "'", dtypeTok));
        }

        return ParseResult.ok(dtype);
    }
}
