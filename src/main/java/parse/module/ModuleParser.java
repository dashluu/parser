package parse.module;

import ast.ASTNode;
import exceptions.ErrMsg;
import lex.LexReader;
import lex.LexResult;
import lex.LexStatus;
import lex.Lexer;
import parse.branch.IfElseParser;
import parse.branch.WhileParser;
import parse.control_transfer.BreakParser;
import parse.control_transfer.ContParser;
import parse.decl_stmt.DeclStmtParser;
import parse.decl_stmt.DeclStmtSemanChecker;
import parse.dtype.DtypeParser;
import parse.dtype.DtypeSemanChecker;
import parse.expr.ExprParser;
import parse.expr.ExprSemanChecker;
import parse.function.*;
import parse.control_transfer.RetParser;
import parse.scope.ScopeParser;
import parse.stmt.StmtParser;
import parse.utils.*;
import toks.Tok;
import toks.TokType;
import parse.utils.ParseContext;

import java.io.IOException;

public class ModuleParser {
    private final LexReader lexReader;
    private final Lexer lexer;
    private final TokMatcher tokMatcher = new TokMatcher();
    private final DtypeParser dtypeParser = new DtypeParser();
    private final DtypeSemanChecker dtypeSemanChecker = new DtypeSemanChecker();
    private final SemiChecker semiChecker = new SemiChecker();
    private final ExprSemanChecker exprSemanChecker = new ExprSemanChecker();
    private final ExprParser exprParser = new ExprParser();
    private final DeclStmtSemanChecker declStmtSemanChecker = new DeclStmtSemanChecker();
    private final DeclStmtParser declStmtParser = new DeclStmtParser();
    private final RetParser retParser = new RetParser();
    private final BreakParser breakParser = new BreakParser();
    private final ContParser contParser = new ContParser();
    private final StmtParser stmtParser = new StmtParser();
    private final IfElseParser ifElseParser = new IfElseParser();
    private final WhileParser whileParser = new WhileParser();
    private final FunHeadParser funHeadParser = new FunHeadParser();
    private final FunHeadSemanChecker funHeadSemanChecker = new FunHeadSemanChecker();
    private final FunDefParser funDefParser = new FunDefParser();
    private final ScopeParser scopeParser = new ScopeParser();

    public ModuleParser(LexReader lexReader) {
        this.lexReader = lexReader;
        lexer = new Lexer(lexReader);
    }

    /**
     * Resolves the dependencies between the components.
     */
    public void init() {
        tokMatcher.init(lexer);
        dtypeParser.init(lexer, tokMatcher);
        semiChecker.init(tokMatcher);
        exprParser.init(lexer, tokMatcher, dtypeParser, exprSemanChecker);
        declStmtSemanChecker.init(dtypeSemanChecker);
        declStmtParser.init(tokMatcher, dtypeParser, exprParser, declStmtSemanChecker);
        retParser.init(tokMatcher, exprParser);
        breakParser.init(tokMatcher);
        contParser.init(tokMatcher);
        stmtParser.init(tokMatcher, semiChecker, exprParser, declStmtParser, retParser, breakParser, contParser);
        ifElseParser.init(tokMatcher, semiChecker, exprParser, scopeParser);
        whileParser.init(tokMatcher, semiChecker, exprParser, scopeParser);
        funHeadSemanChecker.init(dtypeSemanChecker);
        funHeadParser.init(tokMatcher, dtypeParser, funHeadSemanChecker);
        funDefParser.init(funHeadParser, scopeParser);
        scopeParser.init(lexReader, tokMatcher, stmtParser, funDefParser, ifElseParser, whileParser);
    }

    /**
     * Parses a module.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the module.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseModule(ParseContext context) throws IOException {
        ParseResult<ASTNode> moduleResult = scopeParser.parseScope(context);
        if (moduleResult.getStatus() == ParseStatus.ERR) {
            return moduleResult;
        } else if (moduleResult.getStatus() == ParseStatus.FAIL) {
            Tok errTok = moduleResult.getFailTok();
            return context.raiseErr(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
        }

        // Check if the end of stream is reached
        // If not, there is a syntax error
        LexResult<Tok> lexResult = lexer.lookahead(context);
        if (lexResult.getStatus() != LexStatus.OK) {
            // Lexer always yields an OK or error status
            return context.raiseErr(lexResult.getErrMsg());
        } else {
            Tok errTok = lexResult.getData();
            if (errTok.getTokType() != TokType.EOS) {
                return context.raiseErr(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
            }
        }

        return moduleResult;
    }
}
