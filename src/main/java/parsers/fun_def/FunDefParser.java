package parsers.fun_def;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.RetASTNode;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import keywords.KeywordTable;
import parsers.scope.ScopeParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;
import types.TypeTable;

import java.io.IOException;

public class FunDefParser {
    private FunHeadSyntaxPass headSyntaxPass;
    private FunHeadASTPass headAstPass;
    private ScopeParser scopeParser;
    private static final ParseErr ERR = ParseErr.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param headSyntaxPass an object that checks the function definition's syntax.
     * @param headAstPass    an object that constructs the function definition's AST.
     * @param scopeParser    an object that parses code components inside a function scope.
     */
    public void init(FunHeadSyntaxPass headSyntaxPass, FunHeadASTPass headAstPass, ScopeParser scopeParser) {
        this.headSyntaxPass = headSyntaxPass;
        this.headAstPass = headAstPass;
        this.scopeParser = scopeParser;
    }

    /**
     * Parses a function definition, constructs an AST for it, and checks its semantics.
     *
     * @param funScope the scope surrounding the function definition.
     * @return a ParseResult object as the result of parsing a function definition.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseFunDef(Scope funScope) throws IOException {
        SyntaxBuff syntaxBuff = new SyntaxBuff();
        ParseResult<SyntaxInfo> syntaxResult = headSyntaxPass.eatFunHead(syntaxBuff, funScope);
        if (syntaxResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (syntaxResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(syntaxResult.getFailTok());
        }

        ParseResult<ASTNode> astResult = headAstPass.doFunHead(syntaxBuff, funScope);
        if (astResult.getStatus() == ParseStatus.ERR) {
            return astResult;
        }

        Scope bodyScope = new Scope(funScope);
        FunDefASTNode funDefNode = (FunDefASTNode) astResult.getData();
        TypeInfo retType = funDefNode.getDtype();
        bodyScope.setRetType(retType);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(bodyScope);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return bodyResult;
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return ERR.raise(new ErrMsg("Invalid body for function '" + funDefNode.getTok().getVal() + "'",
                    bodyResult.getFailTok()));
        }

        // Check if a return statement is missing
        // Valid only if the return type isn't void
        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        Tok idTok = funDefNode.getTok();
        if (!bodyNode.getRetFlag()) {
            if (retType != TypeTable.VOID) {
                return ERR.raise(new ErrMsg("Missing a return statement in the function '" + idTok.getVal() + "'",
                        idTok));
            } else {
                // Add a dummy return if a return statement is missing and the return type is void
                Tok retTok = new Tok(KeywordTable.RET_KW, TokType.RET);
                RetASTNode retNode = new RetASTNode(retTok, TypeTable.VOID);
                bodyNode.addChild(retNode);
            }
        }

        funDefNode.setBodyNode(bodyNode);
        return astResult;
    }
}
