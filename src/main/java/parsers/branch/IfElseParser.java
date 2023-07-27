package parsers.branch;

import ast.*;
import exceptions.ErrMsg;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import parsers.utils.ParseContext;
import parsers.utils.Scope;
import parsers.utils.ScopeStack;

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
        IfElseASTNode ifElseNode = new IfElseASTNode(result.getData().getTok());
        IfASTNode ifNode = (IfASTNode) result.getData();
        ifElseNode.addChild(ifNode);
        ifElseNode.setRetFlag(ifElseNode.getRetFlag() && ifNode.getRetFlag());
        boolean end = false;

        do {
            result = parseBranch(TokType.ELIF, context, false);
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            } else if (result.getStatus() == ParseStatus.OK) {
                ifNode = (IfASTNode) result.getData();
                ifElseNode.addChild(ifNode);
                ifElseNode.setRetFlag(ifElseNode.getRetFlag() && ifNode.getRetFlag());
            } else {
                result = parseElse();
                if (result.getStatus() == ParseStatus.ERR) {
                    return result;
                } else if (!(end = result.getStatus() == ParseStatus.FAIL)) {
                    ElseASTNode elseNode = (ElseASTNode) result.getData();
                    ifElseNode.addChild(elseNode);
                    ifElseNode.setRetFlag(ifElseNode.getRetFlag() && elseNode.getRetFlag());
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
        elseNode.setRetFlag(bodyNode.getRetFlag());
        scopeStack.pop();
        return ParseResult.ok(elseNode);
    }
}
