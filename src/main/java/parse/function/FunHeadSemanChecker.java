package parse.function;

import ast.*;
import exceptions.ErrMsg;
import parse.scope.FunScope;
import parse.scope.Scope;
import parse.scope.ScopeStack;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import symbols.FunInfo;
import symbols.ParamInfo;
import symbols.SymbolTable;
import toks.Tok;
import types.TypeInfo;
import types.VoidType;

public class FunHeadSemanChecker {
    private ParseContext context;

    /**
     * Checks the semantics of a function header.
     *
     * @param funDefNode the function definition AST's root.
     * @param context    the parsing context.
     * @return a ParseResult object as the result of checking the semantics of the function header.
     */
    public ParseResult<ASTNode> checkSeman(FunDefASTNode funDefNode, ParseContext context) {
        this.context = context;
        // Check function id
        ParseResult<FunInfo> idResult = checkId(funDefNode.getIdNode());
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        FunInfo funInfo = idResult.getData();
        // Check the function signature
        ParseResult<ASTNode> funSignResult = checkFunSign(funDefNode.getSignNode(), funInfo);
        if (funSignResult.getStatus() == ParseStatus.ERR) {
            return funSignResult;
        }

        // Update the return type
        funDefNode.setDtype(funSignResult.getData().getDtype());
        return ParseResult.ok(funDefNode);
    }

    /**
     * Checks if a function signature is valid, which includes the parameter list and the return type.
     *
     * @param funSignNode the AST node associated with the function signature.
     * @param funInfo     the function object to be updated in the symbol table.
     * @return a ParseResult object as the result of checking the function signature.
     */
    private ParseResult<ASTNode> checkFunSign(FunSignASTNode funSignNode, FunInfo funInfo) {
        // Check the parameter list
        ParamListASTNode paramListNode = funSignNode.getParamListNode();
        ParseResult<ASTNode> paramListResult = checkParamList(paramListNode, funInfo);
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        }

        // Check the return type
        DtypeASTNode retDtypeNode = funSignNode.getRetDtypeNode();
        TypeInfo retDtype;
        if (retDtypeNode == null) {
            retDtype = VoidType.getInst();
        } else {
            // Check the return type
            ParseResult<TypeInfo> retDtypeResult = checkDtype(retDtypeNode);
            if (retDtypeResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            retDtype = retDtypeResult.getData();
            retDtypeNode.setDtype(retDtype);
        }

        // Set the return data type for the function scope
        FunScope funScope = (FunScope) context.getScope();
        funScope.setRetDtype(retDtype);
        funInfo.setDtype(retDtype);
        funSignNode.setDtype(retDtype);
        return ParseResult.ok(funSignNode);
    }

    /**
     * Checks if a function identifier is valid.
     *
     * @param idNode the AST node containing the function identifier.
     * @return a ParseResult object as the result of checking the function identifier.
     */
    private ParseResult<FunInfo> checkId(IdASTNode idNode) {
        Tok idTok = idNode.getTok();
        String id = idTok.getVal();
        // Check if the function id is a data type since the id cannot be a keyword
        TypeInfo dtype = context.getTypeTable().getType(id);
        if (dtype != null) {
            return context.raiseErr(new ErrMsg("Data type '" + id + "' cannot be used as a function identifier", idTok));
        }

        // Check if the function id has been defined
        SymbolTable symbolTable = context.getScope().getSymbolTable();
        if (symbolTable.getLocalSymbol(id) != null) {
            return context.raiseErr(new ErrMsg("Identifier '" + id + "' cannot be redeclared", idTok));
        }

        // Create a new function
        FunInfo funInfo = new FunInfo(id, null);
        symbolTable.registerSymbol(funInfo);
        return ParseResult.ok(funInfo);
    }

    /**
     * Checks a function's parameter list.
     *
     * @param paramListNode the AST node associated with the parameter list.
     * @param funInfo       the function symbol.
     * @return a ParseResult object as the result of checking the parameter list.
     */
    private ParseResult<ASTNode> checkParamList(ParamListASTNode paramListNode, FunInfo funInfo) {
        ParseResult<TypeInfo> paramResult;
        TypeInfo paramDtype;
        ScopeStack scopeStack = context.getScopeStack();
        // Peeking from scope stack is the same as getting the current scope from context
        Scope funScope = new FunScope(scopeStack.peek(), null);
        scopeStack.push(funScope);

        for (ASTNode paramDeclNode : paramListNode) {
            paramResult = checkParam((ParamDeclASTNode) paramDeclNode, funScope);
            if (paramResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            paramDtype = paramResult.getData();
            funInfo.addParamDtype(paramDtype);
        }

        return ParseResult.ok(paramListNode);
    }

    /**
     * Checks if a parameter declaration, including its name and data type, is valid.
     *
     * @param paramDeclNode the AST node associated with the parameter declaration.
     * @param paramScope    the scope containing the parameter.
     * @return a ParseResult object as the result of checking the parameter declaration.
     */
    private ParseResult<TypeInfo> checkParam(ParamDeclASTNode paramDeclNode, Scope paramScope) {
        // Check if the parameter has been declared
        IdASTNode nameNode = paramDeclNode.getIdNode();
        Tok nameTok = nameNode.getTok();
        String name = nameTok.getVal();
        SymbolTable symbolTable = paramScope.getSymbolTable();
        if (symbolTable.getLocalSymbol(name) != null) {
            return context.raiseErr(new ErrMsg("Parameter '" + name + "' cannot be redeclared", nameTok));
        }

        // Check the parameter's data type
        DtypeASTNode dtypeNode = paramDeclNode.getDtypeNode();
        ParseResult<TypeInfo> dtypeResult = checkDtype(dtypeNode);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        }

        TypeInfo dtype = dtypeResult.getData();
        // Add parameter to the symbol table
        ParamInfo paramInfo = new ParamInfo(name, dtype);
        symbolTable.registerSymbol(paramInfo);
        nameNode.setDtype(dtype);
        dtypeNode.setDtype(dtype);
        paramDeclNode.setDtype(dtype);
        return ParseResult.ok(dtype);
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
}
