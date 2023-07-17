package parsers.branch;

import ast.ASTNode;
import parsers.utils.ParseResult;
import parsers.utils.Scope;
import toks.TokType;

import java.io.IOException;

public class WhileParser extends CondBranchParser {
    /**
     * Parses a while loop.
     *
     * @param brScope the scope surrounding the while loop.
     * @return a ParseResult object as the result of parsing the while loop.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseWhile(Scope brScope) throws IOException {
        return parseBranch(TokType.WHILE, brScope);
    }
}
