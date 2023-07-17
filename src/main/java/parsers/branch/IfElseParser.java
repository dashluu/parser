package parsers.branch;

import ast.ASTNode;
import ast.ElseASTNode;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class IfElseParser extends CondBranchParser {
    /**
     * Parses a sequence of if-elif-else blocks.
     *
     * @param brScope the scope surrounding the if-elif-else block sequence.
     * @return a ParseResult object as the result of parsing the if-elif-else block sequence.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseIfElse(ScopeASTNode brScopeNode, Scope brScope) throws IOException {
        this.brScope = brScope;

        ParseResult<ASTNode> result = parseBranch(TokType.IF, brScope);
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.FAIL) {
            return result;
        }

        brScopeNode.addChild(result.getData());
        boolean end = false;

        do {
            result = parseBranch(TokType.ELIF, brScope);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            } else if (result.getStatus() == ParseStatus.OK) {
                brScopeNode.addChild(result.getData());
            } else {
                result = parseElse();
                if (result.getStatus() == ParseStatus.ERR) {
                    return result;
                } else if (!(end = result.getStatus() == ParseStatus.FAIL)) {
                    brScopeNode.addChild(result.getData());
                }
            }
        } while (!end);

        // Do not change the return value to result
        // If-elif-else does not necessarily end with an elif or else block
        return ParseResult.ok(null);
    }

    /**
     * Parses an else block, which has no condition.
     *
     * @return a ParseResult object as the result of parsing the else block.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseElse() throws IOException {
        // keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.ELSE);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getData());
        }

        ElseASTNode elseNode = new ElseASTNode(kwResult.getData());
        // Parse the body
        Scope bodyScope = new Scope(brScope);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(bodyScope);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Invalid branch body", bodyResult.getFailTok()));
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        elseNode.setBodyNode(bodyNode);
        return ParseResult.ok(elseNode);
    }
}
