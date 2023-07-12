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
    private Scope scope;
    private final TypeTable typeTable = TypeTable.getInst();
    private final ParseErr err = ParseErr.getInst();

    /**
     * Constructs an AST for a function header.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    public ParseResult<ASTNode> doFunHead(SyntaxBuff syntaxBuff, Scope scope) {
        this.syntaxBuff = syntaxBuff;
        this.scope = scope;
        ParseResult<ASTNode> idResult = doFunId();
        if (idResult.getStatus() == ParseStatus.ERR) {
            return idResult;
        }

        ParseResult<ASTNode> paramListResult = doParamList();
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

        FunDefASTNode funDefNode = (FunDefASTNode) idResult.getData();
        funDefNode.setDtype(retType);
        ParamListASTNode paramListNode = (ParamListASTNode) paramListResult.getData();
        funDefNode.setParamListNode(paramListNode);
        // Update the return type of the function in the symbol table
        SymbolTable symbolTable = scope.getSymbolTable();
        String id = funDefNode.getTok().getVal();
        SymbolInfo symbol = symbolTable.getSymbol(id);
        symbol.setDtype(retType);
        return idResult;
    }

    /**
     * Constructs an AST node for a function definition.
     *
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doFunId() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok idTok = syntaxInfo.getTok();
        // Check if the function id has been defined
        String id = idTok.getVal();
        SymbolTable symbolTable = scope.getSymbolTable();
        if (symbolTable.getSymbol(id) != null) {
            return err.raise(new ErrMsg("'" + id + "' is already defined", idTok));
        }

        // Update the block memory
        long blockMem = MemSys.getBlockMem();
        MemSys.updateBlockMem();
        // Create a new function
        FunInfo funInfo = new FunInfo(id, null, blockMem);
        symbolTable.registerSymbol(funInfo);
        return ParseResult.ok(new FunDefASTNode(idTok, null, blockMem));
    }

    /**
     * Constructs an AST for a parameter list.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doParamList() {
        ParamListASTNode paramListNode = new ParamListASTNode();
        ParseResult<ASTNode> paramResult;
        boolean firstParam = true;
        int i = 0;
        // '('
        syntaxBuff.forward();

        while (syntaxBuff.peek().getTag() != SyntaxTag.RPAREN) {
            if (!firstParam) {
                // ','
                syntaxBuff.forward();
            }
            paramResult = doParam(i);
            if (paramResult.getStatus() == ParseStatus.ERR) {
                return paramResult;
            }
            paramListNode.addChild(paramResult.getData());
            firstParam = false;
            ++i;
        }

        // ')'
        syntaxBuff.forward();
        return ParseResult.ok(paramListNode);
    }

    /**
     * Constructs an AST node for a parameter.
     *
     * @param i the index of the parameter in the list.
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doParam(int i) {
        // Name
        SyntaxInfo nameInfo = syntaxBuff.forward();
        // Data type
        SyntaxInfo dtypeInfo = syntaxBuff.forward();
        Tok dtypeTok = dtypeInfo.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = typeTable.getType(dtypeId);
        if (dtype == null) {
            return err.raise(new ErrMsg("Invalid parameter's data type '" + dtypeId + "'", dtypeTok));
        }

        // Check if the parameter's name has been defined
        SymbolTable symbolTable = scope.getSymbolTable();
        Tok nameTok = nameInfo.getTok();
        String name = nameTok.getVal();
        if (symbolTable.getSymbol(name) != null) {
            return err.raise(new ErrMsg("'" + name + "' is already defined", nameTok));
        }

        // Create a new parameter
        symbolTable.registerSymbol(new ParamInfo(name, dtype, i));
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
        TypeInfo dtype = typeTable.getType(dtypeId);
        if (dtype == null) {
            return err.raise(new ErrMsg("Invalid return type '" + dtypeId + "'", dtypeTok));
        }

        return ParseResult.ok(dtype);
    }
}
