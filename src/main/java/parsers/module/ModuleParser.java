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
import parsers.expr.ExprParser;
import parsers.expr.ExprSemanChecker;
import parsers.function.*;
import parsers.ret.RetParser;
import parsers.scope.ScopeParser;
import parsers.stmt.StmtParser;
import parsers.parse_utils.*;
import toks.Tok;
import toks.TokType;
import utils.ParseContext;

import java.io.IOException;

public class ModuleParser {
    private final Lexer lexer;
    private final TokParser tokParser = new TokParser();
    private final ExprSemanChecker exprSemanChecker = new ExprSemanChecker();
    private final ExprParser exprParser = new ExprParser();
    private final DeclSemanChecker declSemanChecker = new DeclSemanChecker();
    private final DeclParser declParser = new DeclParser();
    private final RetParser retParser = new RetParser();
    private final StmtParser stmtParser = new StmtParser();
    private final IfElseParser ifElseParser = new IfElseParser();
    private final WhileParser whileParser = new WhileParser();
    private final FunHeadParser funHeadParser = new FunHeadParser();
    private final FunHeadSemanChecker funHeadSemanChecker = new FunHeadSemanChecker();
    private final FunDefParser funDefParser = new FunDefParser();
    private final ScopeParser scopeParser = new ScopeParser();

    public ModuleParser(Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Resolves the dependencies between the components.
     */
    public void init() {
        tokParser.init(lexer);
        exprParser.init(lexer, tokParser, exprSemanChecker);
        declParser.init(tokParser, exprParser, declSemanChecker);
        retParser.init(tokParser, exprParser);
        stmtParser.init(tokParser, exprParser, declParser, retParser);
        ifElseParser.init(tokParser, exprParser, scopeParser);
        whileParser.init(tokParser, exprParser, scopeParser);
        funHeadParser.init(tokParser, funHeadSemanChecker);
        funDefParser.init(funHeadParser, scopeParser);
        scopeParser.init(tokParser, stmtParser, funDefParser, ifElseParser, whileParser);
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
            return ParseErr.raise(new ErrMsg("Invalid syntax at '" + errTok.getVal() + "'", errTok));
        }

        // Check if the end of stream is reached
        // If not, there is a syntax error
        LexResult<Tok> lexResult = lexer.lookahead(context);
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
