package parsers.scope;

import ast.ASTNode;
import ast.ASTNodeType;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import parsers.branch.IfElseParser;
import parsers.branch.WhileParser;
import parsers.function.FunDefParser;
import parsers.stmt.StmtParser;
import parsers.parse_utils.*;
import toks.Tok;
import toks.TokType;
import utils.ParseContext;
import utils.Scope;
import utils.ScopeStack;

import java.io.IOException;

public class ScopeParser {
    private TokParser tokParser;
    private StmtParser stmtParser;
    private FunDefParser funDefParser;
    private IfElseParser ifElseParser;
    private WhileParser whileParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser    a parser that consumes valid tokens.
     * @param stmtParser   a statement parser.
     * @param funDefParser a function definition parser.
     * @param ifElseParser an if-elif-else sequence parser.
     * @param whileParser  a while-loop parser.
     */
    public void init(TokParser tokParser, StmtParser stmtParser, FunDefParser funDefParser,
                     IfElseParser ifElseParser, WhileParser whileParser) {
        this.tokParser = tokParser;
        this.stmtParser = stmtParser;
        this.funDefParser = funDefParser;
        this.ifElseParser = ifElseParser;
        this.whileParser = whileParser;
    }

    /**
     * Parses code components, including declarations, if-else, loops, etc. and checks their semantics in a block.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a code block.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseBlock(ParseContext context) throws IOException {
        // Try parsing '{'
        ParseResult<Tok> bracketResult = tokParser.parseTok(TokType.LBRACKETS, context);
        if (bracketResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bracketResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(bracketResult.getFailTok());
        }

        // Try parsing code in a new scope
        Scope newScope = new Scope(context.getScope());
        ScopeStack scopeStack = context.getScopeStack();
        scopeStack.push(newScope);
        ParseResult<ASTNode> scopeResult = parseScope(context);
        // No need to check if it failed since the result is either an error or OK
        if (scopeResult.getStatus() == ParseStatus.ERR) {
            return scopeResult;
        }

        // Try parsing '}'
        bracketResult = tokParser.parseTok(TokType.RBRACKETS, context);
        if (bracketResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bracketResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Missing '}'", bracketResult.getFailTok()));
        }

        scopeStack.pop();
        return scopeResult;
    }

    /**
     * Parses code components, including declarations, if-else, loops, etc. and checks their semantics in a scope.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a scope.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseScope(ParseContext context) throws IOException {
        ParseStatus status;
        ParseResult<ASTNode> stmtResult, funDefResult, blockResult, ifElseResult, whileResult;
        ASTNode stmtNode, funDefNode, whileNode;
        ScopeASTNode blockNode;
        ScopeASTNode scopeNode = new ScopeASTNode();
        boolean end = false;

        while (!end) {
            // Try parsing a function definition
            funDefResult = funDefParser.parseFunDef(context);
            status = funDefResult.getStatus();
            if (status == ParseStatus.ERR) {
                return funDefResult;
            } else if (!(end = status == ParseStatus.FAIL)) {
                funDefNode = funDefResult.getData();
                scopeNode.addChild(funDefNode);
            }

            if (end) {
                // Try parsing a sequence of if-elif-else blocks
                // The blocks are added to the scope if successful
                ifElseResult = ifElseParser.parseIfElse(scopeNode, context);
                status = ifElseResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return ifElseResult;
                }
                end = status == ParseStatus.FAIL;
            }

            if (end) {
                // Try parsing a while loop
                whileResult = whileParser.parseWhile(context);
                status = whileResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return whileResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    whileNode = whileResult.getData();
                    scopeNode.addChild(whileNode);
                }
            }

            if (end) {
                // Try parsing a block
                blockResult = parseBlock(context);
                status = blockResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return blockResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    blockNode = (ScopeASTNode) blockResult.getData();
                    scopeNode.addChild(blockNode);
                    scopeNode.setRetFlag(scopeNode.getRetFlag() || blockNode.getRetFlag());
                }
            }

            if (end) {
                // Try parsing a statement
                stmtResult = stmtParser.parseStmt(context);
                status = stmtResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return stmtResult;
                } else if (status != ParseStatus.EMPTY && !(end = status == ParseStatus.FAIL)) {
                    stmtNode = stmtResult.getData();
                    // Ignore statements following a return statement
                    if (!scopeNode.getRetFlag()) {
                        scopeNode.addChild(stmtNode);
                        if (stmtNode.getNodeType() == ASTNodeType.RET) {
                            // The scope has a return statement
                            scopeNode.setRetFlag(true);
                        }
                    }
                }
            }

            if (end) {
                return ParseResult.ok(scopeNode);
            }
        }

        return ParseResult.ok(scopeNode);
    }
}
