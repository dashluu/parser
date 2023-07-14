package parsers.scope;

import ast.ASTNode;
import ast.ASTNodeType;
import ast.ScopeASTNode;
import exceptions.ErrMsg;
import parsers.branch.IfElseParser;
import parsers.fun_def.FunDefParser;
import parsers.stmt.StmtParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ScopeParser {
    private TokParser tokParser;
    private StmtParser stmtParser;
    private FunDefParser funDefParser;
    private IfElseParser ifElseParser;
    private static final ParseErr err = ParseErr.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param tokParser    a parser that consumes valid tokens.
     * @param stmtParser   a statement parser.
     * @param funDefParser a function definition parser.
     * @param ifElseParser an if-elif-else sequence parser.
     */
    public void init(TokParser tokParser, StmtParser stmtParser, FunDefParser funDefParser, IfElseParser ifElseParser) {
        this.tokParser = tokParser;
        this.stmtParser = stmtParser;
        this.funDefParser = funDefParser;
        this.ifElseParser = ifElseParser;
    }

    /**
     * Parses code components, including declarations, if-else, loops, etc. and checks their semantics in a block.
     *
     * @param scope the scope that surrounds the new block.
     * @return a ParseResult object as the result of parsing a code block.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseBlock(Scope scope) throws IOException {
        // Try parsing '{'
        ParseResult<Tok> bracketResult = tokParser.parseTok(TokType.LBRACKETS);
        if (bracketResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bracketResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(bracketResult.getFailTok());
        }

        // Try parsing code in a new scope
        Scope newScope = new Scope(scope, scope.getRetType());
        ParseResult<ASTNode> scopeResult = parseScope(newScope);
        // No need to check if it failed since the result is either an error or OK
        if (scopeResult.getStatus() == ParseStatus.ERR) {
            return scopeResult;
        }

        // Try parsing '}'
        bracketResult = tokParser.parseTok(TokType.RBRACKETS);
        if (bracketResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bracketResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Missing '}'", bracketResult.getFailTok()));
        }

        return scopeResult;
    }

    /**
     * Parses code components, including declarations, if-else, loops, etc. and checks their semantics in a scope.
     *
     * @param scope the parent scope of the current scope.
     * @return a ParseResult object as the result of parsing a scope.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseScope(Scope scope) throws IOException {
        ParseStatus status;
        ParseResult<ASTNode> stmtResult, funDefResult, blockResult, ifElseResult;
        ASTNode stmtNode, funDefNode;
        ScopeASTNode blockNode;
        ScopeASTNode scopeNode = new ScopeASTNode();
        boolean end = false;

        while (!end) {
            // Try parsing a statement
            stmtResult = stmtParser.parseStmt(scope);
            status = stmtResult.getStatus();
            if (status == ParseStatus.ERR) {
                return ParseResult.err();
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

            if (end) {
                // Try parsing a function definition
                funDefResult = funDefParser.parseFunDef(scope);
                status = funDefResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return ParseResult.err();
                } else if (!(end = status == ParseStatus.FAIL)) {
                    funDefNode = funDefResult.getData();
                    scopeNode.addChild(funDefNode);
                }
            }

            if (end) {
                // Try parsing a sequence of if-elif-else blocks
                // The blocks are added to the scope if successful
                ifElseResult = ifElseParser.parseIfElse(scopeNode, scope);
                status = ifElseResult.getStatus();
                if (status == ParseStatus.ERR) {
                    return ifElseResult;
                }
                end = status == ParseStatus.FAIL;
            }

            if (end) {
                // Try parsing a block
                blockResult = parseBlock(scope);
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
                return ParseResult.ok(scopeNode);
            }
        }

        return ParseResult.ok(scopeNode);
    }
}
