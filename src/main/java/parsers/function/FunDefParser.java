package parsers.function;

import ast.*;
import exceptions.ErrMsg;
import keywords.KeywordTable;
import parsers.scope.ScopeParser;
import parsers.parse_utils.*;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;
import types.TypeTable;
import utils.ParseContext;
import utils.Scope;
import utils.ScopeStack;

import java.io.IOException;

public class FunDefParser {
    private FunHeadParser funHeadParser;
    private ScopeParser scopeParser;

    /**
     * Initializes the dependencies.
     *
     * @param funHeadParser a function header parser.
     * @param scopeParser   a scope parser.
     */
    public void init(FunHeadParser funHeadParser, ScopeParser scopeParser) {
        this.funHeadParser = funHeadParser;
        this.scopeParser = scopeParser;
    }

    /**
     * Parses a function definition, constructs an AST for it, and checks its semantics.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a function definition.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseFunDef(ParseContext context) throws IOException {
        ParseResult<ASTNode> funHeadResult = funHeadParser.parseFunHead(context);
        if (funHeadResult.getStatus() == ParseStatus.ERR || funHeadResult.getStatus() == ParseStatus.FAIL) {
            return funHeadResult;
        }

        TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) funHeadResult.getData();
        FunDefASTNode funDefNode = (FunDefASTNode) typeAnnNode.getLeft();
        TypeInfo retType = funDefNode.getDtype();
        Scope bodyScope = new Scope(context.getScope());
        ScopeStack scopeStack = context.getScopeStack();
        scopeStack.push(bodyScope);
        bodyScope.setRetDtype(retType);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(context);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return bodyResult;
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Invalid body for function '" + funDefNode.getTok().getVal() + "'",
                    bodyResult.getFailTok()));
        }

        // Check if a return statement is missing, this is only valid if the return type is void
        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        Tok idTok = funDefNode.getTok();
        if (!bodyNode.getRetFlag()) {
            if (retType != TypeTable.VOID) {
                return ParseErr.raise(new ErrMsg("Missing a return statement in the function '" +
                        idTok.getVal() + "'", idTok));
            } else {
                // Add a dummy return if a return statement is missing and the return type is void
                Tok retTok = new Tok(KeywordTable.RET, TokType.RET);
                RetASTNode retNode = new RetASTNode(retTok, TypeTable.VOID);
                bodyNode.addChild(retNode);
            }
        }

        funDefNode.setBodyNode(bodyNode);
        // Pop body's scope
        scopeStack.pop();
        // Pop parameters' scope
        scopeStack.pop();
        return funHeadResult;
    }
}
