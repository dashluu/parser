package parsers.branch;

import ast.ASTNode;
import ast.ElseASTNode;
import ast.IfElseASTNode;
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
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the if-elif-else block sequence.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseIfElse(ParseContext context) throws IOException {
        this.context = context;

        ParseResult<ASTNode> result = parseBranch(TokType.IF, context, false);
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.FAIL) {
            return result;
        }

        // Create an if-else node to hold if-else sequence
        IfElseASTNode ifElseNode = new IfElseASTNode();
        ifElseNode.addChild(result.getData());
        boolean end = false;

        do {
            result = parseBranch(TokType.ELIF, context, false);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            } else if (result.getStatus() == ParseStatus.OK) {
                ifElseNode.addChild(result.getData());
            } else {
                result = parseElse();
                if (result.getStatus() == ParseStatus.ERR) {
                    return result;
                } else if (!(end = result.getStatus() == ParseStatus.FAIL)) {
                    ifElseNode.addChild(result.getData());
                }
            }
        } while (!end);

        return ParseResult.ok(ifElseNode);
    }

    /**
     * Parses an else block, which has no condition.
     *
     * @return a ParseResult object as the result of parsing the else block.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseElse() throws IOException {
        // keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.ELSE, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getData());
        }

        ElseASTNode elseNode = new ElseASTNode(kwResult.getData());
        // Parse the body
        Scope bodyScope = new Scope(context.getScope());
        ScopeStack scopeStack = context.getScopeStack();
        scopeStack.push(bodyScope);
        ParseResult<ASTNode> bodyResult = scopeParser.parseBlock(context);
        if (bodyResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bodyResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid branch body", bodyResult.getFailTok()));
        }

        ScopeASTNode bodyNode = (ScopeASTNode) bodyResult.getData();
        elseNode.setBodyNode(bodyNode);
        scopeStack.pop();
        return ParseResult.ok(elseNode);
    }
}
