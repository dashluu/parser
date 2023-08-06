package parsers.branch;

import ast.ASTNode;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.scope.ScopeType;
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
