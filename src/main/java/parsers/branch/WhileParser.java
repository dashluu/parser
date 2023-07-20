package parsers.branch;

import ast.ASTNode;
import utils.Context;
import parsers.utils.ParseResult;
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
    public ParseResult<ASTNode> parseWhile(Context context) throws IOException {
        return parseBranch(TokType.WHILE, context);
    }
}
