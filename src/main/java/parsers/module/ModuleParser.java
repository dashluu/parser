package parsers.module;

import ast.ASTNode;
import exceptions.ErrMsg;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import parsers.branch.BranchParser;
import parsers.decl.DeclASTPass;
import parsers.decl.DeclParser;
import parsers.decl.DeclSemanChecker;
import parsers.decl.DeclSyntaxPass;
import parsers.expr.ExprASTPass;
import parsers.expr.ExprParser;
import parsers.expr.ExprSemanChecker;
import parsers.expr.ExprSyntaxPass;
import parsers.fun_def.FunDefParser;
import parsers.fun_def.FunHeadASTPass;
import parsers.fun_def.FunHeadSyntaxPass;
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
    private final DeclSyntaxPass declSyntaxPass;
    private final DeclASTPass declASTPass;
    private final DeclSemanChecker declSemanChecker;
    private final DeclParser declParser;
    private final RetParser retParser;
    private final StmtParser stmtParser;
    private final BranchParser brParser;
    private final FunHeadSyntaxPass funHeadSyntaxPass;
    private final FunHeadASTPass funHeadASTPass;
    private final FunDefParser funDefParser;
    private final ScopeParser scopeParser;
    private final ParseErr err = ParseErr.getInst();

    public ModuleParser(Lexer lexer) {
        this.lexer = lexer;
        tokParser = new TokParser();
        exprSyntaxPass = new ExprSyntaxPass();
        exprASTPass = new ExprASTPass();
        exprSemanChecker = new ExprSemanChecker();
        exprParser = new ExprParser();
        declSyntaxPass = new DeclSyntaxPass();
        declASTPass = new DeclASTPass();
        declSemanChecker = new DeclSemanChecker();
        declParser = new DeclParser();
        retParser = new RetParser();
        stmtParser = new StmtParser();
        brParser = new BranchParser();
        funHeadSyntaxPass = new FunHeadSyntaxPass();
        funHeadASTPass = new FunHeadASTPass();
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
        declSyntaxPass.init(tokParser, exprSyntaxPass);
        declASTPass.init(exprASTPass);
        declSemanChecker.init(exprSemanChecker);
        declParser.init(declSyntaxPass, declASTPass, declSemanChecker);
        retParser.init(tokParser, exprParser);
        stmtParser.init(lexer, tokParser, exprParser, declParser, retParser);
        brParser.init(tokParser, exprParser, scopeParser);
        funHeadSyntaxPass.init(tokParser);
        funDefParser.init(funHeadSyntaxPass, funHeadASTPass, scopeParser);
        scopeParser.init(tokParser, stmtParser, funDefParser, brParser);
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
            return err.raise(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
        }

        // Check if the end of stream is reached
        // If not, there is a syntax error
        LexResult<Tok> lexResult = lexer.lookahead();
        if (lexResult.getStatus() != LexStatus.OK) {
            // Lexer always yields an OK or error status
            return err.raise(lexResult.getErrMsg());
        } else {
            Tok errTok = lexResult.getData();
            if (errTok.getType() != TokType.EOS) {
                return err.raise(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
            }
        }

        return moduleResult;
    }
}
