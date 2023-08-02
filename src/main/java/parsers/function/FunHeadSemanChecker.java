package parsers.function;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.ParamListASTNode;
import ast.TypeAnnASTNode;
import exceptions.ErrMsg;
import parsers.utils.*;
import symbols.FunInfo;
import symbols.ParamInfo;
import symbols.SymbolTable;
import toks.Tok;
import types.TypeInfo;
import parsers.utils.ParseContext;
import parsers.utils.Scope;
import parsers.utils.ScopeStack;

public class FunHeadSemanChecker {
    private ParseContext context;

    /**
     * Checks the semantics of a function header.
     *
     * @param typeAnnNode the type annotation node that contains a function declaration node on the left and a return
     *                    type node on the right.
     * @param context     the parsing context.
     * @return a ParseResult object as the result of checking the semantics of a function header.
     */
    public ParseResult<ASTNode> checkSeman(TypeAnnASTNode typeAnnNode, ParseContext context) {
        this.context = context;
        FunDefASTNode funDefNode = (FunDefASTNode) typeAnnNode.getLeft();
        // Check function id
        ParseResult<FunInfo> idResult = checkId(funDefNode);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        FunInfo funInfo = idResult.getData();
        // Check the parameter list
        ParamListASTNode paramListNode = funDefNode.getParamListNode();
        ParseResult<ASTNode> paramListResult = checkParamList(funInfo, paramListNode);
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        }

        // Check the return type
        ASTNode retDtypeNode = typeAnnNode.getDtypeNode();
        ParseResult<TypeInfo> retDtypeResult = checkDtype(retDtypeNode);
        if (retDtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        TypeInfo retDtype = retDtypeResult.getData();
        funInfo.setDtype(retDtype);
        typeAnnNode.setDtype(retDtype);
        funDefNode.setDtype(retDtype);
        retDtypeNode.setDtype(retDtype);
        return ParseResult.ok(typeAnnNode);
    }

    /**
     * Checks if a function identifier is valid.
     *
     * @param funDefNode the AST node associated with the function declaration.
     * @return a ParseResult object as the result of checking the function identifier.
     */
    private ParseResult<FunInfo> checkId(ASTNode funDefNode) {
        Tok idTok = funDefNode.getTok();
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
     * @param funInfo       the function symbol.
     * @param paramListNode the AST node associated with the parameter list.
     * @return a ParseResult object as the result of checking the parameter list.
     */
    private ParseResult<ASTNode> checkParamList(FunInfo funInfo, ParamListASTNode paramListNode) {
        ParseResult<TypeInfo> paramResult;
        TypeInfo paramDtype;
        ScopeStack scopeStack = context.getScopeStack();
        // Peeking from scope stack is the same as getting the current scope from context
        Scope paramScope = new Scope(scopeStack.peek());
        scopeStack.push(paramScope);

        for (ASTNode typeAnnNode : paramListNode) {
            paramResult = checkParam((TypeAnnASTNode) typeAnnNode, paramScope);
            if (paramResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            }
            paramDtype = paramResult.getData();
            funInfo.addParamDtype(paramDtype);
        }

        return ParseResult.ok(paramListNode);
    }

    /**
     * Checks if a parameter, including its name and data type, is valid.
     *
     * @param typeAnnNode the type annotation node that contains a parameter declaration node on the left and a data
     *                    type node on the right.
     * @param paramScope  the scope containing the parameter.
     * @return a ParseResult object as the result of checking the parameter.
     */
    private ParseResult<TypeInfo> checkParam(TypeAnnASTNode typeAnnNode, Scope paramScope) {
        // Check if the parameter has been defined
        ASTNode paramDeclNode = typeAnnNode.getLeft();
        Tok nameTok = paramDeclNode.getTok();
        String name = nameTok.getVal();
        SymbolTable symbolTable = paramScope.getSymbolTable();
        if (symbolTable.getLocalSymbol(name) != null) {
            return context.raiseErr(new ErrMsg("'" + name + "' cannot be redeclared", nameTok));
        }

        // Check the parameter's data type
        ASTNode dtypeNode = typeAnnNode.getDtypeNode();
        ParseResult<TypeInfo> dtypeResult = checkDtype(dtypeNode);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        }

        TypeInfo dtype = dtypeResult.getData();
        // Add parameter to the symbol table
        ParamInfo paramInfo = new ParamInfo(name, dtype);
        symbolTable.registerSymbol(paramInfo);
        typeAnnNode.setDtype(dtype);
        paramDeclNode.setDtype(dtype);
        dtypeNode.setDtype(dtype);
        return ParseResult.ok(dtype);
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
            return context.raiseErr(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }
        return ParseResult.ok(dtype);
    }
}
