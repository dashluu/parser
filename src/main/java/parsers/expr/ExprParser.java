package parsers.expr;

import ast.ASTNode;
import parsers.utils.*;

import java.io.IOException;

public class ExprParser {
    private ExprSyntaxPass syntaxPass;
    private ExprASTPass astPass;
    private ExprSemanChecker semanChecker;

    /**
     * Initializes the dependencies.
     *
     * @param syntaxPass   an object that checks the expression's syntax.
     * @param astPass      an object that constructs the expression's AST.
     * @param semanChecker an object that checks the expression's semantics.
     */
    public void init(ExprSyntaxPass syntaxPass, ExprASTPass astPass, ExprSemanChecker semanChecker) {
        this.syntaxPass = syntaxPass;
        this.astPass = astPass;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a general expression in a scope, constructs an AST for it, and checks its semantics.
     *
     * @param scope the scope surrounding the expression.
     * @return a ParseResult object as the result of parsing the expression.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseExpr(Scope scope) throws IOException {
        SyntaxBuff syntaxBuff = new SyntaxBuff();
        ParseResult<SyntaxInfo> syntaxResult = syntaxPass.eatExpr(syntaxBuff);
        if (syntaxResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (syntaxResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(syntaxResult.getFailTok());
        }

        ParseResult<ASTNode> astResult = astPass.doExpr(syntaxBuff, scope);
        // No need to check if it fails since AST construction only returns ERR or OK
        if (astResult.getStatus() == ParseStatus.ERR) {
            return astResult;
        }

        return semanChecker.checkSeman(astResult.getData(), scope);
    }
}
