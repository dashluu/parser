package parsers.module;

import ast.ASTNode;
import exceptions.ErrMsg;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import parsers.branch.IfElseParser;
import parsers.branch.WhileParser;
import parsers.decl.DeclParser;
import parsers.decl.DeclSemanChecker;
import parsers.expr.ExprASTPass;
import parsers.expr.ExprParser;
import parsers.expr.ExprSemanChecker;
import parsers.expr.ExprSyntaxPass;
import parsers.fun_def.*;
import parsers.ret.RetParser;
import parsers.scope.ScopeParser;
import parsers.stmt.StmtParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ModuleParser {
    private final Lexer lexer;
    private final TokParser tokParser;
    private final ExprSyntaxPass exprSyntaxPass;
    private final ExprASTPass exprASTPass;
    private final ExprSemanChecker exprSemanChecker;
    private final ExprParser exprParser;
    private final DeclSemanChecker declSemanChecker;
    private final DeclParser declParser;
    private final RetParser retParser;
    private final StmtParser stmtParser;
    private final IfElseParser ifElseParser;
    private final WhileParser whileParser;
    private final FunHeadParser funHeadParser;
    private final FunHeadSemanChecker funHeadSemanChecker;
    private final FunDefParser funDefParser;
    private final ScopeParser scopeParser;

    public ModuleParser(Lexer lexer) {
        this.lexer = lexer;
        tokParser = new TokParser();
        exprSyntaxPass = new ExprSyntaxPass();
        exprASTPass = new ExprASTPass();
        exprSemanChecker = new ExprSemanChecker();
        exprParser = new ExprParser();
        declSemanChecker = new DeclSemanChecker();
        declParser = new DeclParser();
        retParser = new RetParser();
        stmtParser = new StmtParser();
        ifElseParser = new IfElseParser();
        whileParser = new WhileParser();
        funHeadParser = new FunHeadParser();
        funHeadSemanChecker = new FunHeadSemanChecker();
        funDefParser = new FunDefParser();
        scopeParser = new ScopeParser();
    }

    /**
     * Resolves the dependencies between the components.
     */
    public void init() {
        tokParser.init(lexer);
        exprSyntaxPass.init(lexer, tokParser);
        exprParser.init(exprSyntaxPass, exprASTPass, exprSemanChecker);
        declParser.init(tokParser, exprParser, declSemanChecker);
        retParser.init(tokParser, exprParser);
        stmtParser.init(lexer, tokParser, exprParser, declParser, retParser);
        ifElseParser.init(tokParser, exprParser, scopeParser);
        whileParser.init(tokParser, exprParser, scopeParser);
        funHeadParser.init(tokParser, funHeadSemanChecker);
        funDefParser.init(funHeadParser, scopeParser);
        scopeParser.init(tokParser, stmtParser, funDefParser, ifElseParser, whileParser);
    }

    /**
     * Parses a module.
     *
     * @return a ParseResult object as the result of parsing the module.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseModule() throws IOException {
        Scope globalScope = new Scope(null);
        ParseResult<ASTNode> moduleResult = scopeParser.parseScope(globalScope);
        if (moduleResult.getStatus() == ParseStatus.ERR) {
            return moduleResult;
        } else if (moduleResult.getStatus() == ParseStatus.FAIL) {
            Tok errTok = moduleResult.getFailTok();
            return ParseErr.raise(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
        }

        // Check if the end of stream is reached
        // If not, there is a syntax error
        LexResult<Tok> lexResult = lexer.lookahead();
        if (lexResult.getStatus() != LexStatus.OK) {
            // Lexer always yields an OK or error status
            return ParseErr.raise(lexResult.getErrMsg());
        } else {
            Tok errTok = lexResult.getData();
            if (errTok.getType() != TokType.EOS) {
                return ParseErr.raise(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
            }
        }

        return moduleResult;
    }
}
