package parse.branch;

import ast.ASTNode;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.scope.ScopeType;
import toks.TokType;

import java.io.IOException;

public class WhileParser extends CondBranchParser {
    /**
     * Parses a while loop.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing the while loop.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseWhile(ParseContext context) throws IOException {
        return parseBranch(TokType.WHILE, ScopeType.LOOP, context);
    }
}
