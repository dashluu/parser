package parsers.branch;

import ast.*;
import exceptions.ErrMsg;
import parsers.expr.ExprParser;
import parsers.scope.ScopeParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import types.TypeTable;
import parsers.utils.ParseContext;
import parsers.utils.Scope;
import parsers.utils.ScopeStack;

import java.io.IOException;

public abstract class CondBranchParser {
    protected TokParser tokParser;
    protected ExprParser exprParser;
    protected ScopeParser scopeParser;
    protected ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser   a parser that consumes valid tokens.
     * @param exprParser  an expression parser.
     * @param scopeParser a scope parser.
     */
    public void init(TokParser tokParser, ExprParser exprParser, ScopeParser scopeParser) {
        this.tokParser = tokParser;
        this.exprParser = exprParser;
        this.scopeParser = scopeParser;
    }

    /**
     * Parses a conditional branch block, which includes its condition and body.
     *
     * @param tokType the token type of the branch keyword.
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the conditional branch block.
     * @throws IOException if there is an IO exception.
     */
    protected ParseResult<ASTNode> parseBranch(TokType tokType, ParseContext context) throws IOException {
        this.context = context;
        // Parse the condition
        ParseResult<ASTNode> condResult = parseCond(tokType);
        if (condResult.getStatus() == ParseStatus.ERR) {
            return condResult;
        } else if (condResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(condResult.getFailTok());
        }

        BranchNode brNode = (BranchNode) condResult.getData();
        // Parse the body
        Scope bodyScope = new Scope(context.getScope());
        ScopeStack scopeStack = context.getScopeStack();
        scopeStack.push(bodyScope);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(context);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return bodyResult;
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid branch body", bodyResult.getFailTok()));
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        brNode.setBodyNode(bodyNode);
        scopeStack.pop();
        return ParseResult.ok(brNode);
    }

    /**
     * Parses a branch condition.
     *
     * @param tokType the token type of the branch keyword.
     * @return a ParseResult object as the result of parsing a branch condition.
     * @throws IOException if there is an IO exception.
     */
    protected ParseResult<ASTNode> parseCond(TokType tokType) throws IOException {
        // keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(tokType, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getData());
        }

        // '('
        ParseResult<Tok> lparenResult = tokParser.parseTok(TokType.LPAREN, context);
        if (lparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (lparenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(lparenResult.getData());
        }

        // Expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid branch expression", exprResult.getFailTok()));
        }

        // ')'
        ParseResult<Tok> rparenResult = tokParser.parseTok(TokType.RPAREN, context);
        if (rparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (rparenResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing ')'", rparenResult.getFailTok()));
        }

        ASTNode exprNode = exprResult.getData();
        // Check if expression has a boolean value
        if (!exprNode.getDtype().equals(TypeTable.BOOL)) {
            return context.raiseErr(new ErrMsg("Branch condition's expression is not of boolean type",
                    lparenResult.getData()));
        }

        Tok kwTok = kwResult.getData();
        BranchNode brNode = switch (tokType) {
            case IF -> new IfASTNode(kwTok);
            case ELIF -> new ElifASTNode(kwTok);
            default -> new WhileASTNode(kwTok);
        };
        brNode.setCondNode(exprNode);
        return ParseResult.ok(brNode);
    }
}
