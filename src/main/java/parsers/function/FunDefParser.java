package parsers.function;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.ScopeASTNode;
import ast.TypeAnnASTNode;
import exceptions.ErrMsg;
import parsers.scope.ScopeParser;
import parsers.utils.*;
import types.TypeInfo;

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
            return context.raiseErr(new ErrMsg("Invalid body for function '" + funDefNode.getTok().getVal() + "'",
                    bodyResult.getFailTok()));
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        funDefNode.setBodyNode(bodyNode);
        // Pop body's scope
        scopeStack.pop();
        // Pop parameters' scope
        scopeStack.pop();
        return funHeadResult;
    }
}
