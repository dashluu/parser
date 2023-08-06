package parsers.function;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.RetASTNode;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import keywords.KeywordTable;
import parsers.scope.*;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;
import types.VoidType;

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

        FunDefASTNode funDefNode = (FunDefASTNode) funHeadResult.getData();
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(ScopeType.SIMPLE, context);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return bodyResult;
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid body for function '" + funDefNode.getTok().getVal() + "'",
                    bodyResult.getFailTok()));
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        funDefNode.setBodyNode(bodyNode);
        // Pop function scope that was pushed earlier when the parameter list and the return type was parsed
        ScopeStack scopeStack = context.getScopeStack();
        Scope funScope = scopeStack.pop();

        // Check if the return statement is present
        if (funScope.getRetState() != RetState.PRESENT) {
            SrcPos funDefEnd = funDefNode.getSrcRange().getEndPos();
            if (funDefNode.getDtype() != VoidType.getInst()) {
                return context.raiseErr(new ErrMsg("Missing a return statement", funDefEnd));
            } else {
                // Add a dummy return statement to the body
                Tok retTok = new Tok(KeywordTable.RET, TokType.RET, new SrcRange(funDefEnd));
                RetASTNode retNode = new RetASTNode(retTok, VoidType.getInst());
                bodyNode.addChild(retNode);
            }
        }

        return ParseResult.ok(funDefNode);
    }
}
