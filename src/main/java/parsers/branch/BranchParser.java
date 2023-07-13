package parsers.branch;

import ast.*;
import exceptions.ErrMsg;
import parsers.expr.ExprParser;
import parsers.scope.ScopeParser;
import parsers.utils.*;
import symbols.LabelGen;
import toks.Tok;
import toks.TokType;
import types.TypeTable;

import java.io.IOException;

public class BranchParser {
    private TokParser tokParser;
    private ExprParser exprParser;
    private ScopeParser scopeParser;
    // Branch scope
    private Scope brScope;
    private final ParseErr err = ParseErr.getInst();

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
     * Parses a branch sequence, that is, a sequence of if-elif-else blocks.
     *
     * @param brScope the scope surrounding the branch sequence.
     * @return a ParseResult object as the result of parsing the branch sequence.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseBranchSeq(ScopeASTNode brScopeNode, Scope brScope) throws IOException {
        this.brScope = brScope;
        ParseResult<ASTNode> result = parseBlock(TokType.IF);
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.FAIL) {
            return result;
        }

        boolean end = false;

        do {
            result = parseBlock(TokType.ELIF);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            } else if (result.getStatus() == ParseStatus.OK) {
                brScopeNode.addChild(result.getData());
            } else {
                result = parseBlock(TokType.ELSE);
                if (result.getStatus() == ParseStatus.ERR) {
                    return result;
                } else if (!(end = result.getStatus() == ParseStatus.FAIL)) {
                    brScopeNode.addChild(result.getData());
                }
            }
        } while (!end);

        return ParseResult.ok(result.getData());
    }

    /**
     * Parses a branch block, which includes its condition and body.
     *
     * @param tokType the token type of the branch keyword.
     * @return a ParseResult object as the result of parsing the branch block.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseBlock(TokType tokType) throws IOException {
        // Parse the condition
        ParseResult<ASTNode> condResult = parseCond(tokType);
        if (condResult.getStatus() == ParseStatus.ERR) {
            return condResult;
        } else if (condResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(condResult.getFailTok());
        }

        BranchNode brNode = (BranchNode) condResult.getData();
        // Parse the body
        Scope bodyScope = new Scope(brScope);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(bodyScope);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Invalid branch body", bodyResult.getFailTok()));
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
    private ParseResult<ASTNode> parseCond(TokType tokType) throws IOException {
        // 'keyword'
        ParseResult<Tok> kwResult = tokParser.parseTok(tokType);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getData());
        }

        // '('
        ParseResult<Tok> lparenResult = tokParser.parseTok(TokType.LPAREN);
        if (lparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (lparenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(lparenResult.getData());
        }

        // Expression
        ParseResult<ASTNode> exprResult = exprParser.parseExpr(brScope);
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        } else if (exprResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Invalid branch expression", exprResult.getFailTok()));
        }

        // ')'
        ParseResult<Tok> rparenResult = tokParser.parseTok(TokType.RPAREN);
        if (rparenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (rparenResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Missing ')'", rparenResult.getFailTok()));
        }

        ASTNode exprNode = exprResult.getData();
        // Check if expression has a boolean value
        if (!exprNode.getDtype().equals(TypeTable.BOOL)) {
            return err.raise(new ErrMsg("Branch condition's expression is not of boolean type",
                    lparenResult.getData()));
        }

        Tok kwTok = kwResult.getData();
        int label = LabelGen.getBlockLabel();
        BranchNode brNode = switch (tokType) {
            case IF -> new IfASTNode(kwTok, label);
            case ELIF -> new ElifASTNode(kwTok, label);
            default -> new ElseASTNode(kwTok, label);
        };
        brNode.setCondNode(exprNode);
        return ParseResult.ok(brNode);
    }
}
