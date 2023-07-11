package parsers.decl;

import ast.ASTNode;
import parsers.utils.*;

import java.io.IOException;

public class DeclParser {
    private DeclSyntaxPass syntaxPass;
    private DeclASTPass astPass;
    private DeclSemanChecker semanChecker;

    /**
     * Initializes the dependencies.
     *
     * @param syntaxPass   an object that checks the right-hand side(rhs) expression's syntax.
     * @param astPass      an object that constructs the rhs expression's AST.
     * @param semanChecker an object that checks the rhs expression's semantics.
     */
    public void init(DeclSyntaxPass syntaxPass, DeclASTPass astPass, DeclSemanChecker semanChecker) {
        this.syntaxPass = syntaxPass;
        this.astPass = astPass;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a declaration statement in a scope, constructs an AST for it, and checks its semantics.
     *
     * @param scope the scope surrounding the declaration statement.
     * @return a ParseResult object as the result of parsing the declaration statement.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseDecl(Scope scope) throws IOException {
        SyntaxBuff syntaxBuff = new SyntaxBuff();
        ParseResult<SyntaxInfo> syntaxResult = syntaxPass.eatDecl(syntaxBuff);
        if (syntaxResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (syntaxResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(syntaxResult.getFailTok());
        }

        ParseResult<ASTNode> astResult = astPass.doDecl(syntaxBuff, scope);
        if (astResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        return semanChecker.checkSeman(astResult.getData(), scope);
    }
}
