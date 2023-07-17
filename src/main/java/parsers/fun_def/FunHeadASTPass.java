package parsers.fun_def;

import ast.*;
import exceptions.ErrMsg;
import parsers.utils.*;
import symbols.*;
import toks.Tok;
import types.TypeInfo;
import types.TypeTable;

public class FunHeadASTPass {
    private SyntaxBuff syntaxBuff;
    private Scope funScope;
    private static final TypeTable TYPE_TABLE = TypeTable.getInst();

    /**
     * Constructs an AST for a function header.
     *
     * @param syntaxBuff a buffer containing syntax information.
     * @param funScope   the scope surrounding the function header.
     * @return a ParseResult object as the result of constructing the AST.
     */
    public ParseResult<ASTNode> doFunHead(SyntaxBuff syntaxBuff, Scope funScope) {
        this.syntaxBuff = syntaxBuff;
        this.funScope = funScope;
        ParseResult<Pair<FunInfo, ASTNode>> idResult = doFunId();
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        Pair<FunInfo, ASTNode> pair = idResult.getData();
        FunInfo funInfo = pair.first();
        FunDefASTNode funDefNode = (FunDefASTNode) pair.second();
        ParseResult<ASTNode> paramListResult = doParamList(funInfo);
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        }

        TypeInfo retType;
        ParseResult<TypeInfo> retTypeResult = doRetType();
        if (retTypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (retTypeResult.getStatus() == ParseStatus.FAIL) {
            retType = TypeTable.VOID;
        } else {
            retType = retTypeResult.getData();
        }

        funDefNode.setDtype(retType);
        ParamListASTNode paramListNode = (ParamListASTNode) paramListResult.getData();
        funDefNode.setParamListNode(paramListNode);
        // Update the return type of the function in the symbol table
        SymbolTable symbolTable = funScope.getSymbolTable();
        String id = funDefNode.getTok().getVal();
        SymbolInfo symbol = symbolTable.getClosureSymbol(id);
        symbol.setDtype(retType);
        return ParseResult.ok(funDefNode);
    }

    /**
     * Constructs an AST node for a function definition and initializes a new function symbol.
     *
     * @return a ParseResult object as the result of constructing the AST node and initializing a new function symbol.
     */
    private ParseResult<Pair<FunInfo, ASTNode>> doFunId() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok idTok = syntaxInfo.getTok();
        // Check if the function id has been defined
        String id = idTok.getVal();
        SymbolTable symbolTable = funScope.getSymbolTable();
        if (symbolTable.getLocalSymbol(id) != null) {
            return ParseErr.raise(new ErrMsg("'" + id + "' cannot be redeclared", idTok));
        }

        // Create a new function
        FunInfo funInfo = new FunInfo(id, null);
        symbolTable.registerSymbol(funInfo);
        ASTNode funDefNode = new FunDefASTNode(idTok, null);
        return ParseResult.ok(new Pair<>(funInfo, funDefNode));
    }

    /**
     * Constructs an AST for a parameter list.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doParamList(FunInfo funInfo) {
        ParamListASTNode paramListNode = new ParamListASTNode();
        ParseResult<ASTNode> paramResult;
        ASTNode paramNode;
        boolean firstParam = true;
        Scope paramScope = new Scope(funScope);
        // '('
        syntaxBuff.forward();

        while (syntaxBuff.peek().getTag() != SyntaxTag.RPAREN) {
            if (!firstParam) {
                // ','
                syntaxBuff.forward();
            }

            paramResult = doParam(paramScope);
            if (paramResult.getStatus() == ParseStatus.ERR) {
                return paramResult;
            }

            paramNode = paramResult.getData();
            funInfo.addParamDtype(paramNode.getDtype());
            paramListNode.addChild(paramNode);
            firstParam = false;
        }

        // ')'
        syntaxBuff.forward();
        return ParseResult.ok(paramListNode);
    }

    /**
     * Constructs an AST node for a parameter.
     *
     * @param paramScope the scope surrounding the parameters, which is different from the scope surrounding the function.
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doParam(Scope paramScope) {
        // Name
        SyntaxInfo nameInfo = syntaxBuff.forward();
        // Data type
        SyntaxInfo dtypeInfo = syntaxBuff.forward();
        Tok dtypeTok = dtypeInfo.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = TYPE_TABLE.getType(dtypeId);
        if (dtype == null) {
            return ParseErr.raise(new ErrMsg("Invalid parameter's data type '" + dtypeId + "'", dtypeTok));
        }

        // Check if the parameter's name has been defined
        SymbolTable symbolTable = paramScope.getSymbolTable();
        Tok nameTok = nameInfo.getTok();
        String name = nameTok.getVal();
        SymbolInfo symbol = symbolTable.getClosureSymbol(name);
        if (symbol != null && symbol.getSymbolType() == SymbolType.PARAM) {
            return ParseErr.raise(new ErrMsg("'" + name + "' cannot be redeclared", nameTok));
        }

        // Create a new parameter
        symbol = new ParamInfo(name, dtype);
        symbolTable.registerSymbol(symbol);
        return ParseResult.ok(new ParamDeclASTNode(nameTok, dtype));
    }

    /**
     * Checks if the return type is a valid data type.
     *
     * @return a ParseResult object as the result of checking the return type.
     */
    private ParseResult<TypeInfo> doRetType() {
        if (syntaxBuff.peek().getTag() != SyntaxTag.TYPE_ID) {
            return ParseResult.fail(null);
        }

        // Return type's id
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok dtypeTok = syntaxInfo.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = TYPE_TABLE.getType(dtypeId);
        if (dtype == null) {
            return ParseErr.raise(new ErrMsg("Invalid return type '" + dtypeId + "'", dtypeTok));
        }

        return ParseResult.ok(dtype);
    }
}
