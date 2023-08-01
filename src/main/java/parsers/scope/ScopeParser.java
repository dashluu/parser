package parsers.scope;

import ast.ASTNode;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import lexers.LexReader;
import parsers.branch.IfElseParser;
import parsers.branch.WhileParser;
import parsers.function.FunDefParser;
import parsers.stmt.StmtParser;
import parsers.utils.*;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ScopeParser {
    private LexReader lexReader;
    private TokParser tokParser;
    private StmtParser stmtParser;
    private FunDefParser funDefParser;
    private IfElseParser ifElseParser;
    private WhileParser whileParser;

    /**
     * Initializes the dependencies.
     *
     * @param lexReader    a lexeme reader.
     * @param tokParser    a parser that consumes valid tokens.
     * @param stmtParser   a statement parser.
     * @param funDefParser a function definition parser.
     * @param ifElseParser an if-elif-else sequence parser.
     * @param whileParser  a while-loop parser.
     */
    public void init(LexReader lexReader, TokParser tokParser, StmtParser stmtParser, FunDefParser funDefParser,
                     IfElseParser ifElseParser, WhileParser whileParser) {
        this.lexReader = lexReader;
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
        ParseResult<Tok> curlyResult = tokParser.parseTok(TokType.LCURLY, context);
        if (curlyResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (curlyResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(curlyResult.getFailTok());
        }

        Tok curlyTok = curlyResult.getData();
        SrcPos blockStartPos = curlyTok.getSrcRange().getStartPos();
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
        curlyResult = tokParser.parseTok(TokType.RCURLY, context);
        if (curlyResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (curlyResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing '}'", curlyResult.getFailTok()));
        }

        curlyTok = curlyResult.getData();
        SrcPos blockEndPos = curlyTok.getSrcRange().getEndPos();
        SrcRange blockRange = new SrcRange(blockStartPos, blockEndPos);
        ScopeASTNode blockNode = (ScopeASTNode) scopeResult.getData();
        blockNode.setSrcRange(blockRange);
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
        ScopeASTNode scopeNode = new ScopeASTNode(null);
        SrcPos scopeStartPos = lexReader.getSrcPos();
        SrcPos scopeEndPos;
        SrcRange scopeRange;
        boolean end = false;

        while (!end) {
            // Try parsing a function definition
            funDefResult = funDefParser.parseFunDef(context);
            status = funDefResult.getStatus();
            if (status == ParseStatus.ERR) {
                return funDefResult;
            } else if (!(end = status == ParseStatus.FAIL)) {
                scopeNode.addChild(funDefResult.getData());
            }

            if (end) {
                // Try parsing a sequence of if-elif-else blocks
                // The blocks are added to the scope if successful
                ifElseResult = ifElseParser.parseIfElse(context);
                status = ifElseResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return ifElseResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    scopeNode.addChild(ifElseResult.getData());
                }
            }

            if (end) {
                // Try parsing a while loop
                whileResult = whileParser.parseWhile(context);
                status = whileResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return whileResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    scopeNode.addChild(whileResult.getData());
                }
            }

            if (end) {
                // Try parsing a block
                blockResult = parseBlock(context);
                status = blockResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return blockResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    scopeNode.addChild(blockResult.getData());
                }
            }

            if (end) {
                // Try parsing a statement
                stmtResult = stmtParser.parseStmt(context);
                status = stmtResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return stmtResult;
                } else if (!(end = status == ParseStatus.FAIL)) {
                    if (status != ParseStatus.EMPTY) {
                        scopeNode.addChild(stmtResult.getData());
                    }
                }
            }
        }

        scopeEndPos = lexReader.getSrcPos();
        scopeRange = new SrcRange(scopeStartPos, scopeEndPos);
        scopeNode.setSrcRange(scopeRange);
        return ParseResult.ok(scopeNode);
    }
}
