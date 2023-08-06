package parsers.module;

import ast.ASTNode;
import exceptions.ErrMsg;
import lexers.LexReader;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import parsers.branch.IfElseParser;
import parsers.branch.WhileParser;
import parsers.control_transfer.BreakParser;
import parsers.control_transfer.ContParser;
import parsers.decl_stmt.DeclStmtParser;
import parsers.decl_stmt.DeclStmtSemanChecker;
import parsers.dtype.DtypeParser;
import parsers.expr.ExprParser;
import parsers.expr.ExprSemanChecker;
import parsers.function.*;
import parsers.control_transfer.RetParser;
import parsers.scope.ScopeParser;
import parsers.stmt.StmtParser;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import parsers.utils.ParseContext;

import java.io.IOException;

public class ModuleParser {
    private final LexReader lexReader;
    private final Lexer lexer;
    private final TokParser tokParser = new TokParser();
    private final DtypeParser dtypeParser = new DtypeParser();
    private final SemiParser semiParser = new SemiParser();
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
        tokParser.init(lexer);
        dtypeParser.init(tokParser);
        semiParser.init(tokParser);
        exprParser.init(lexer, tokParser, exprSemanChecker);
        declStmtParser.init(tokParser, dtypeParser, exprParser, declStmtSemanChecker);
        retParser.init(tokParser, exprParser);
        breakParser.init(tokParser);
        contParser.init(tokParser);
        stmtParser.init(tokParser, semiParser, exprParser, declStmtParser, retParser, breakParser, contParser);
        ifElseParser.init(tokParser, semiParser, exprParser, scopeParser);
        whileParser.init(tokParser, semiParser, exprParser, scopeParser);
        funHeadParser.init(tokParser, dtypeParser, funHeadSemanChecker);
        funDefParser.init(funHeadParser, scopeParser);
        scopeParser.init(lexReader, tokParser, stmtParser, funDefParser, ifElseParser, whileParser);
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
