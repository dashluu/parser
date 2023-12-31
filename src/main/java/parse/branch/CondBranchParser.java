package parse.branch;

import ast.*;
import exceptions.ErrMsg;
import parse.expr.ExprParser;
import parse.scope.ScopeParser;
import parse.scope.ScopeType;
import parse.utils.*;
import toks.Tok;
import toks.TokType;
import types.BoolType;

import java.io.IOException;

public abstract class CondBranchParser {
    protected TokMatcher tokMatcher;
    protected SemiChecker semiChecker;
    protected ExprParser exprParser;
    protected ScopeParser scopeParser;
    protected ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher  a token matcher.
     * @param semiChecker a trailing semicolon checker.
     * @param exprParser  an expression parser for parsing the branch condition.
     * @param scopeParser a scope parser for parsing the branch body.
     */
    public void init(TokMatcher tokMatcher, SemiChecker semiChecker, ExprParser exprParser, ScopeParser scopeParser) {
        this.tokMatcher = tokMatcher;
        this.semiChecker = semiChecker;
        this.exprParser = exprParser;
        this.scopeParser = scopeParser;
    }

    /**
     * Parses a conditional branch block, which includes its condition and body.
     *
     * @param tokType   the token type of the branch keyword.
     * @param scopeType the scope type of the branch.
     * @param context   the parsing context.
     * @return a ParseResult object as the result of parsing the conditional branch block.
     * @throws IOException if there is an IO exception.
     */
    protected ParseResult<ASTNode> parseBranch(TokType tokType, ScopeType scopeType, ParseContext context)
            throws IOException {
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
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(scopeType, context);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return bodyResult;
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            if (scopeType != ScopeType.LOOP) {
                return context.raiseErr(new ErrMsg("Invalid branch body", bodyResult.getFailTok()));
            }
            // Check for trailing ';'
            return semiChecker.check(condResult, context);
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        brNode.setBodyNode(bodyNode);
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
        ParseResult<Tok> kwResult = tokMatcher.parseTok(tokType, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getData());
        }

        // '('
        ParseResult<Tok> lparenResult = tokMatcher.parseTok(TokType.LPAREN, context);
        if (lparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (lparenResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing '('", lparenResult.getFailTok()));
        }

        // Expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(context);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid branch expression", exprResult.getFailTok()));
        }

        // ')'
        ParseResult<Tok> rparenResult = tokMatcher.parseTok(TokType.RPAREN, context);
        if (rparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (rparenResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing ')'", rparenResult.getFailTok()));
        }

        ASTNode exprNode = exprResult.getData();
        // Check if expression has a boolean value
        if (!exprNode.getDtype().equals(BoolType.getInst())) {
            return context.raiseErr(new ErrMsg("Branch condition's expression is not of boolean type",
                    lparenResult.getData()));
        }

        Tok kwTok = kwResult.getData();
        BranchNode brNode = switch (tokType) {
            case IF, ELIF -> new IfASTNode(kwTok);
            default -> new WhileASTNode(kwTok);
        };
        brNode.setCondNode(exprNode);
        return ParseResult.ok(brNode);
    }
}
